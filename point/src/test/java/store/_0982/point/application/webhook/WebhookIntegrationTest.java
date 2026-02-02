package store._0982.point.application.webhook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.common.WebhookEvents;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.constant.WebhookStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.WebhookLog;
import store._0982.point.domain.event.PaymentCanceledTxEvent;
import store._0982.point.domain.event.PaymentConfirmedTxEvent;
import store._0982.point.infrastructure.pg.PgPaymentCancelJpaRepository;
import store._0982.point.infrastructure.pg.PgPaymentJpaRepository;
import store._0982.point.infrastructure.webhook.WebhookLogJpaRepository;
import store._0982.point.presentation.dto.TossWebhookRequest;
import store._0982.point.support.BaseIntegrationTest;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class WebhookIntegrationTest extends BaseIntegrationTest {

    private static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
            .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
            .build();

    @Autowired
    private TossWebhookFacade tossWebhookFacade;

    @Autowired
    private PgPaymentJpaRepository pgPaymentRepository;

    @Autowired
    private PgPaymentCancelJpaRepository pgPaymentCancelRepository;

    @Autowired
    private WebhookLogJpaRepository webhookLogRepository;

    @MockitoSpyBean
    private TossWebhookService tossWebhookService;

    private UUID orderId;
    private String paymentKey;
    private long amount;
    private PgPayment pgPayment;

    @BeforeEach
    void setUp() {
        webhookLogRepository.deleteAll();
        pgPaymentCancelRepository.deleteAll();
        pgPaymentRepository.deleteAll();

        UUID memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        paymentKey = "test_payment_key";
        amount = 10000L;

        pgPayment = PgPayment.create(memberId, orderId, amount, "테스트 공구");
        pgPaymentRepository.save(pgPayment);
    }

    @Nested
    @DisplayName("웹훅 엔드투엔드 처리")
    class WebhookEndToEnd {

        @Test
        @DisplayName("DONE 상태 웹훅을 처리하고 결제를 완료한다")
        void webhook_payment_done_e2e() throws JsonProcessingException {
            // given
            String webhookId = UUID.randomUUID().toString();
            TossPaymentInfo paymentInfo = createCompletedPaymentInfo();
            TossWebhookRequest request = new TossWebhookRequest(
                    WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                    LocalDateTime.now(),
                    paymentInfo
            );

            // when
            tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);

            // then
            PgPayment updatedPayment = pgPaymentRepository.findByOrderId(orderId).orElseThrow();
            assertThat(updatedPayment.getStatus()).isEqualTo(PgPaymentStatus.COMPLETED);

            WebhookLog webhookLog = webhookLogRepository.findByWebhookId(webhookId).orElseThrow();
            assertThat(webhookLog.getStatus()).isEqualTo(WebhookStatus.SUCCESS);
        }

        @Test
        @DisplayName("CANCELED 상태 웹훅을 처리하고 결제를 환불한다")
        void webhook_payment_canceled_e2e() throws JsonProcessingException {
            // given
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPaymentRepository.save(pgPayment);

            String webhookId = UUID.randomUUID().toString();
            TossPaymentInfo paymentInfo = createCanceledPaymentInfo();
            TossWebhookRequest request = new TossWebhookRequest(
                    WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                    LocalDateTime.now(),
                    paymentInfo
            );

            // when
            tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);

            // then
            PgPayment updatedPayment = pgPaymentRepository.findByOrderId(orderId).orElseThrow();
            assertThat(updatedPayment.getStatus()).isEqualTo(PgPaymentStatus.REFUNDED);

            WebhookLog webhookLog = webhookLogRepository.findByWebhookId(webhookId).orElseThrow();
            assertThat(webhookLog.getStatus()).isEqualTo(WebhookStatus.SUCCESS);
        }

        @Test
        @DisplayName("ABORTED 상태 웹훅을 처리하고 결제를 실패 처리한다")
        void webhook_payment_failed_e2e() throws JsonProcessingException {
            // given
            String webhookId = UUID.randomUUID().toString();
            TossPaymentInfo paymentInfo = createFailedPaymentInfo();
            TossWebhookRequest request = new TossWebhookRequest(
                    WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                    LocalDateTime.now(),
                    paymentInfo
            );

            // when
            tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);

            // then
            PgPayment updatedPayment = pgPaymentRepository.findByOrderId(orderId).orElseThrow();
            assertThat(updatedPayment.getStatus()).isEqualTo(PgPaymentStatus.FAILED);

            WebhookLog webhookLog = webhookLogRepository.findByWebhookId(webhookId).orElseThrow();
            assertThat(webhookLog.getStatus()).isEqualTo(WebhookStatus.SUCCESS);
        }

        @Test
        @DisplayName("PARTIAL_CANCELED 상태 웹훅을 처리하고 부분 환불한다")
        void webhook_partial_canceled_e2e() throws JsonProcessingException {
            // given
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPaymentRepository.save(pgPayment);

            String webhookId = UUID.randomUUID().toString();
            TossPaymentInfo paymentInfo = createPartiallyCanceledPaymentInfo();
            TossWebhookRequest request = new TossWebhookRequest(
                    WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                    LocalDateTime.now(),
                    paymentInfo
            );

            // when
            tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);

            // then
            PgPayment updatedPayment = pgPaymentRepository.findByOrderId(orderId).orElseThrow();
            assertThat(updatedPayment.getStatus()).isEqualTo(PgPaymentStatus.PARTIALLY_REFUNDED);

            WebhookLog webhookLog = webhookLogRepository.findByWebhookId(webhookId).orElseThrow();
            assertThat(webhookLog.getStatus()).isEqualTo(WebhookStatus.SUCCESS);
        }
    }

    @Nested
    @DisplayName("웹훅 로그 REQUIRES_NEW 트랜잭션")
    class WebhookLogTransaction {

        @Test
        @DisplayName("웹훅 처리 실패 시에도 웹훅 로그는 저장된다")
        void webhook_log_saved_even_on_failure() throws JsonProcessingException {
            // given
            String webhookId = UUID.randomUUID().toString();
            TossPaymentInfo paymentInfo = createCompletedPaymentInfo();
            TossWebhookRequest request = new TossWebhookRequest(
                    WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                    LocalDateTime.now(),
                    paymentInfo
            );

            String errorMessage = "결제 조회 실패";
            doThrow(new RuntimeException(errorMessage))
                    .when(tossWebhookService)
                    .processWebhookPayment(any(TossPaymentInfo.class));

            // when & then
            OffsetDateTime now = OffsetDateTime.now();
            assertThatThrownBy(() -> tossWebhookFacade.handleTossWebhook(1, webhookId, now, request))
                    .isInstanceOf(RuntimeException.class);

            WebhookLog webhookLog = webhookLogRepository.findByWebhookId(webhookId).orElseThrow();
            assertThat(webhookLog.getStatus()).isEqualTo(WebhookStatus.FAILED);
            assertThat(webhookLog.getErrorMessage()).contains(errorMessage);
        }

        @Test
        @DisplayName("웹훅 로그는 별도 트랜잭션으로 저장된다")
        void webhook_log_separate_transaction() throws JsonProcessingException {
            // given
            String webhookId = UUID.randomUUID().toString();
            TossPaymentInfo paymentInfo = createCompletedPaymentInfo();
            TossWebhookRequest request = new TossWebhookRequest(
                    WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                    LocalDateTime.now(),
                    paymentInfo
            );

            // when
            tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);

            // then
            WebhookLog webhookLog = webhookLogRepository.findByWebhookId(webhookId).orElseThrow();
            assertThat(webhookLog).isNotNull();
            assertThat(webhookLog.getStatus()).isEqualTo(WebhookStatus.SUCCESS);
        }
    }

    @Nested
    @DisplayName("이벤트 발행 검증")
    class EventPublishing {

        @Test
        @DisplayName("결제 완료 시 PaymentConfirmedTxEvent가 발행된다")
        void payment_confirmed_event_published() throws JsonProcessingException {
            // given
            String webhookId = UUID.randomUUID().toString();
            TossPaymentInfo paymentInfo = createCompletedPaymentInfo();
            TossWebhookRequest request = new TossWebhookRequest(
                    WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                    LocalDateTime.now(),
                    paymentInfo
            );

            // when
            tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);

            // then
            long count = events.stream(PaymentConfirmedTxEvent.class).count();
            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("결제 취소 시 PaymentCanceledTxEvent가 발행된다")
        void payment_canceled_event_published() throws JsonProcessingException {
            // given
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), paymentKey);
            pgPaymentRepository.save(pgPayment);

            String webhookId = UUID.randomUUID().toString();
            TossPaymentInfo paymentInfo = createCanceledPaymentInfo();
            TossWebhookRequest request = new TossWebhookRequest(
                    WebhookEvents.TOSS_PAYMENT_STATUS_CHANGED,
                    LocalDateTime.now(),
                    paymentInfo
            );

            // when
            tossWebhookFacade.handleTossWebhook(1, webhookId, OffsetDateTime.now(), request);

            // then
            long count = events.stream(PaymentCanceledTxEvent.class).count();
            assertThat(count).isEqualTo(1);
        }
    }

    private TossPaymentInfo createCompletedPaymentInfo() {
        return TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.DONE)
                .card(FIXTURE_MONKEY.giveMeOne(TossPaymentInfo.Card.class))
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .build();
    }

    private TossPaymentInfo createCanceledPaymentInfo() {
        TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(amount)
                .cancelReason("고객 요청")
                .canceledAt(OffsetDateTime.now())
                .transactionKey("test_transaction_key")
                .build();

        return TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.CANCELED)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .cancels(List.of(cancelInfo))
                .build();
    }

    private TossPaymentInfo createPartiallyCanceledPaymentInfo() {
        TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(5000L)
                .cancelReason("부분 취소")
                .canceledAt(OffsetDateTime.now())
                .transactionKey("test_transaction_key")
                .build();

        return TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.PARTIAL_CANCELED)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .cancels(List.of(cancelInfo))
                .build();
    }

    private TossPaymentInfo createFailedPaymentInfo() {
        return TossPaymentInfo.builder()
                .paymentKey(paymentKey)
                .orderId(orderId)
                .amount(amount)
                .method("카드")
                .status(TossPaymentInfo.Status.ABORTED)
                .requestedAt(OffsetDateTime.now())
                .failure(TossPaymentInfo.FailureInfo.builder()
                        .code("PAYMENT_FAILED")
                        .message("결제 실패")
                        .build())
                .build();
    }
}
