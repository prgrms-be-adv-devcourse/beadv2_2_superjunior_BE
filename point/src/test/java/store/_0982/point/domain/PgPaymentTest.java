package store._0982.point.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PgPaymentTest {

    private static final int DEFAULT_AMOUNT = 10_000;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("결제 포인트를 생성한다")
    void create() {
        // when
        PgPayment pgPayment = PgPayment.create(memberId, orderId, DEFAULT_AMOUNT);

        // then
        assertThat(pgPayment.getMemberId()).isEqualTo(memberId);
        assertThat(pgPayment.getOrderId()).isEqualTo(orderId);
        assertThat(pgPayment.getAmount()).isEqualTo(DEFAULT_AMOUNT);
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.PENDING);
    }

    @Test
    @DisplayName("결제를 승인 처리한다")
    void markConfirmed() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, DEFAULT_AMOUNT);

        OffsetDateTime approvedAt = OffsetDateTime.now();
        String paymentKey = "test_payment_key";

        // when
        pgPayment.markConfirmed(PaymentMethod.CARD, approvedAt, paymentKey);

        // then
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.COMPLETED);
        assertThat(pgPayment.getPaymentMethod()).isEqualTo(PaymentMethod.CARD);
        assertThat(pgPayment.getApprovedAt()).isEqualTo(approvedAt);
        assertThat(pgPayment.getPaymentKey()).isEqualTo(paymentKey);
    }

    @Test
    @DisplayName("결제를 실패 처리한다")
    void markFailed() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, DEFAULT_AMOUNT);

        // when
        pgPayment.markFailed();

        // then
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.FAILED);
    }

    @Test
    @DisplayName("결제를 환불 대기 처리한다")
    void markRefundPending() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, DEFAULT_AMOUNT);

        // when
        pgPayment.markRefundPending();

        // then
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.REFUND_PENDING);
    }

    @Test
    @DisplayName("결제를 환불 처리한다")
    void markRefunded() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, DEFAULT_AMOUNT);
        pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), "test_payment_key");

        OffsetDateTime refundedAt = OffsetDateTime.now();

        // when
        pgPayment.markRefunded(refundedAt);

        // then
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.REFUNDED);
        assertThat(pgPayment.getRefundedAt()).isEqualTo(refundedAt);
    }
}
