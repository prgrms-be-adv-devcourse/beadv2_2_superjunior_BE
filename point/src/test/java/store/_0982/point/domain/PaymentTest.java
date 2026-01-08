package store._0982.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.point.domain.constant.PaymentStatus;
import store._0982.point.domain.entity.Payment;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {

    @Test
    @DisplayName("결제 포인트를 생성한다")
    void create() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        int amount = 10000;

        // when
        Payment payment = Payment.create(memberId, orderId, amount);

        // then
        assertThat(payment.getMemberId()).isEqualTo(memberId);
        assertThat(payment.getPgOrderId()).isEqualTo(orderId);
        assertThat(payment.getAmount()).isEqualTo(amount);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
    }

    @Test
    @DisplayName("결제를 승인 처리한다")
    void markConfirmed() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.create(memberId, orderId, 10000);

        String paymentMethod = "CARD";
        OffsetDateTime approvedAt = OffsetDateTime.now();
        String paymentKey = "test_payment_key";

        // when
        payment.markConfirmed(paymentMethod, approvedAt, paymentKey);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(payment.getApprovedAt()).isEqualTo(approvedAt);
        assertThat(payment.getPaymentKey()).isEqualTo(paymentKey);
    }

    @Test
    @DisplayName("결제를 실패 처리한다")
    void markFailed() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.create(memberId, orderId, 10000);

        String errorMessage = "카드 승인 실패";

        // when
        payment.markFailed(errorMessage);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("결제를 환불 처리한다")
    void markRefunded() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        Payment payment = Payment.create(memberId, orderId, 10000);
        payment.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

        OffsetDateTime refundedAt = OffsetDateTime.now();
        String cancelReason = "고객 요청";

        // when
        payment.markRefunded(refundedAt, cancelReason);

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        assertThat(payment.getRefundedAt()).isEqualTo(refundedAt);
        assertThat(payment.getRefundMessage()).isEqualTo(cancelReason);
    }
}
