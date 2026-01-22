package store._0982.point.application.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderChangedEvent;
import store._0982.point.application.TossPaymentService;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentCancel;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.infrastructure.pg.PgPaymentCancelJpaRepository;
import store._0982.point.infrastructure.pg.PgPaymentJpaRepository;
import store._0982.point.infrastructure.point.PointBalanceJpaRepository;
import store._0982.point.infrastructure.point.PointTransactionJpaRepository;
import store._0982.point.support.BaseKafkaTest;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderChangedEventListenerTest extends BaseKafkaTest {

    @Autowired
    private PointBalanceJpaRepository pointBalanceRepository;

    @Autowired
    private PointTransactionJpaRepository pointTransactionRepository;

    @Autowired
    private PgPaymentJpaRepository pgPaymentRepository;

    @Autowired
    private PgPaymentCancelJpaRepository pgPaymentCancelRepository;

    @MockitoBean
    private TossPaymentService tossPaymentService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        pgPaymentCancelRepository.deleteAll();
        pgPaymentRepository.deleteAll();
        pointTransactionRepository.deleteAll();
        pointBalanceRepository.deleteAll();

        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("공동구매 실패 이벤트를 수신하면 PG 결제 환불 로직이 실행된다")
    void handleOrderChangedEvent_GroupPurchaseFail_Pg() {
        // given
        long cancelAmount = 10000;
        PgPayment pgPayment = PgPayment.create(memberId, orderId, cancelAmount);
        pgPayment.markConfirmed(PaymentMethod.EASY_PAY, OffsetDateTime.now(), "test_payment_key");
        pgPaymentRepository.save(pgPayment);

        OrderChangedEvent event = new OrderChangedEvent(
                Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault()),
                orderId,
                memberId,
                OrderChangedEvent.Status.GROUP_PURCHASE_FAIL,
                "상품명"
        );

        TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(cancelAmount)
                .cancelReason("공동구매 실패")
                .canceledAt(OffsetDateTime.now())
                .transactionKey(UUID.randomUUID().toString())
                .build();

        TossPaymentInfo tossPaymentInfo = TossPaymentInfo.builder()
                .cancels(List.of(cancelInfo))
                .build();

        when(tossPaymentService.cancelPayment(any(PgPayment.class), any())).thenReturn(tossPaymentInfo);

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CHANGED, event);

        // then
        awaitUntilAsserted(() -> {
            // 1. 환불 상태 확인
            PgPayment payment = pgPaymentRepository.findByOrderId(orderId).orElseThrow();
            assertThat(payment.getStatus()).isEqualTo(PgPaymentStatus.REFUNDED);

            // 2. 취소 내역 확인
            List<PgPaymentCancel> cancels = pgPaymentCancelRepository.findAllByPgPayment(payment);
            assertThat(cancels).singleElement()
                    .extracting(PgPaymentCancel::getCancelAmount, PgPaymentCancel::getCancelReason)
                    .containsExactly(cancelAmount, "공동구매 실패");

            verify(tossPaymentService).cancelPayment(any(PgPayment.class), any());
        });
    }

    @Test
    @DisplayName("공동구매 실패 이벤트를 수신하면 포인트 결제 환불 로직이 실행된다")
    void handleOrderChangedEvent_GroupPurchaseFail_Point() {
        // given
        long paidAmount = 3000;
        long bonusAmount = 2000;
        PointBalance balance = new PointBalance(memberId);
        pointBalanceRepository.save(balance);

        // 포인트 사용 내역 생성 (반환할 대상)
        PointTransaction payment = PointTransaction.used(
                memberId,
                orderId,
                UUID.randomUUID(),
                PointAmount.of(paidAmount, bonusAmount)
        );
        pointTransactionRepository.save(payment);

        OrderChangedEvent event = new OrderChangedEvent(
                Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault()),
                orderId,
                memberId,
                OrderChangedEvent.Status.GROUP_PURCHASE_FAIL,
                "상품명"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CHANGED, event);

        // then
        awaitUntilAsserted(() -> {
            // 1. 포인트 잔액 확인
            PointBalance current = pointBalanceRepository.findByMemberId(memberId).orElseThrow();
            assertThat(current.getPaidBalance()).isEqualTo(paidAmount);
            assertThat(current.getBonusBalance()).isEqualTo(bonusAmount);

            // 2. 포인트 반환 내역 확인
            List<PointTransaction> transactions = pointTransactionRepository.findAllByOrderId(orderId);
            assertThat(transactions)
                    .hasSize(2)  // USED + RETURNED
                    .extracting(PointTransaction::getStatus)
                    .containsExactlyInAnyOrder(
                            PointTransactionStatus.USED,
                            PointTransactionStatus.RETURNED
                    );

            // 3. 반환 금액 확인
            PointTransaction returnPayment = transactions.stream()
                    .filter(p -> p.getStatus() == PointTransactionStatus.RETURNED)
                    .findFirst()
                    .orElseThrow();
            assertThat(returnPayment.getTotalAmount()).isEqualTo(5000);
        });
    }

    @Test
    @DisplayName("공동구매 실패가 아닌 이벤트는 무시된다")
    void handleOrderChangedEvent_OtherStatus() {
        // given
        OrderChangedEvent event = new OrderChangedEvent(
                Clock.fixed(Instant.parse("2024-01-01T10:00:00Z"), ZoneId.systemDefault()),
                orderId,
                memberId,
                OrderChangedEvent.Status.PAYMENT_COMPLETED,
                "상품명"
        );

        // when
        kafkaTemplate.send(KafkaTopics.ORDER_CHANGED, event);

        // then: 2초 동안 DB 상태가 변하지 않아야 함
        await().during(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(pgPaymentRepository.count()).isZero();
                    assertThat(pointTransactionRepository.count()).isZero();
                });
    }
}
