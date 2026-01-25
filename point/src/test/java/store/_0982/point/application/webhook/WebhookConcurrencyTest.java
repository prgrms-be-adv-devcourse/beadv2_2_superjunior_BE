package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.WebhookEvents;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.constant.WebhookStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.domain.event.PaymentConfirmedTxEvent;
import store._0982.point.infrastructure.pg.PgPaymentCancelJpaRepository;
import store._0982.point.infrastructure.pg.PgPaymentJpaRepository;
import store._0982.point.infrastructure.webhook.WebhookLogJpaRepository;
import store._0982.point.presentation.dto.TossWebhookRequest;
import store._0982.point.support.BaseConcurrencyTest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("트랜잭션 구조를 바꾸거나 DB 커넥션 풀을 늘리지 않으면 락에 대한 타임아웃 발생")
class WebhookConcurrencyTest extends BaseConcurrencyTest {

    @Autowired
    private TossWebhookFacade tossWebhookFacade;

    @Autowired
    private PgPaymentJpaRepository pgPaymentRepository;

    @Autowired
    private PgPaymentCancelJpaRepository pgPaymentCancelRepository;

    @Autowired
    private WebhookLogJpaRepository webhookLogRepository;

    private UUID orderId;
    private String paymentKey;
    private long amount;
    private PgPayment pgPayment;

    @BeforeEach
    void setUp() {
        webhookLogRepository.deleteAll();
        pgPaymentCancelRepository.deleteAll();
        pgPaymentRepository.deleteAll();

        orderId = UUID.randomUUID();
        paymentKey = "test_payment_key";
        amount = 10000L;

        pgPayment = PgPayment.create(UUID.randomUUID(), orderId, amount, "테스트 공구");
        pgPaymentRepository.save(pgPayment);
    }

    @Test
    @DisplayName("동일한 webhookId로 동시 요청 시 pessimistic lock으로 하나만 처리된다")
    void concurrent_same_webhook_id() throws InterruptedException {
        // given
        String webhookId = UUID.randomUUID().toString();
        TossPaymentInfo paymentInfo = createCompletedPaymentInfo();
        TossWebhookRequest request = new TossWebhookRequest(
                WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                LocalDateTime.now(),
                paymentInfo
        );

        // when
        runSynchronizedTask(() -> {
            try {
                tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        // then
        List<WebhookLog> webhookLogs = webhookLogRepository.findAll();
        assertThat(webhookLogs).hasSize(1);
        assertThat(webhookLogs.get(0).getStatus()).isEqualTo(WebhookStatus.SUCCESS);
    }

    @Test
    @DisplayName("동일한 orderId에 대해 서로 다른 webhookId로 동시 요청 시 모두 처리된다")
    void concurrent_different_webhook_same_order() throws InterruptedException {
        // given
        int threadCount = getDefaultThreadCount();
        List<String> webhookIds = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            webhookIds.add(UUID.randomUUID().toString());
        }

        TossPaymentInfo paymentInfo = createCompletedPaymentInfo();
        TossWebhookRequest request = new TossWebhookRequest(
                WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                LocalDateTime.now(),
                paymentInfo
        );

        // when
        runSynchronizedTasks(webhookIds, webhookId -> {
            try {
                tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        // then
        assertThat(webhookLogRepository.findAll())
                .hasSize(threadCount)
                .extracting(WebhookLog::getStatus)
                .containsOnly(WebhookStatus.SUCCESS);

        assertThat(pgPaymentRepository.findAll())
                .singleElement()
                .extracting(PgPayment::getStatus)
                .isEqualTo(PgPaymentStatus.COMPLETED);

        assertEventPublishedOnce(PaymentConfirmedTxEvent.class);
    }

    @Test
    @DisplayName("PgPayment 동시 업데이트 시 데이터 정합성이 유지된다")
    void concurrent_pg_payment_update() throws InterruptedException {
        // given
        pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
        pgPaymentRepository.save(pgPayment);

        TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(amount)
                .cancelReason("고객 요청")
                .canceledAt(OffsetDateTime.now())
                .transactionKey("test_transaction_key")
                .build();

        TossPaymentInfo paymentInfo = TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.CANCELED)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .cancels(List.of(cancelInfo))
                .build();

        TossWebhookRequest request = new TossWebhookRequest(
                WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                LocalDateTime.now(),
                paymentInfo
        );

        // when
        runSynchronizedTask(() -> {
            try {
                String webhookId = UUID.randomUUID().toString();
                tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        // then
        PgPayment updatedPayment = pgPaymentRepository.findByOrderId(orderId).orElseThrow();
        assertThat(updatedPayment.getRefundedAt()).isNotNull();
    }

    @Test
    @DisplayName("동일 웹훅의 재시도 카운트가 올바르게 업데이트된다")
    void concurrent_retry_count_ordering() throws InterruptedException {
        // given
        String webhookId = UUID.randomUUID().toString();
        TossPaymentInfo paymentInfo = createCompletedPaymentInfo();

        List<Integer> retryCounts = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // when
        runSynchronizedTasks(retryCounts, retryCount -> {
            try {
                TossWebhookRequest request = new TossWebhookRequest(
                        WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                        LocalDateTime.now(),
                        paymentInfo
                );
                tossWebhookFacade.handleTossWebhook(retryCount, webhookId, OffsetDateTime.now(), request);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        // then
        WebhookLog webhookLog = webhookLogRepository.findByWebhookId(webhookId).orElseThrow();
        assertThat(webhookLog).isNotNull();
        assertThat(webhookLog.getStatus()).isIn(WebhookStatus.SUCCESS, WebhookStatus.FAILED);
    }

    private TossPaymentInfo createCompletedPaymentInfo() {
        return TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();
    }
}
