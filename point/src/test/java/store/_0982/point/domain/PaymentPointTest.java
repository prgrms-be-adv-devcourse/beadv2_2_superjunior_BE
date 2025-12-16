package store._0982.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.point.domain.constant.PaymentPointStatus;
import store._0982.point.domain.entity.PaymentPoint;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentPointTest {

    @Test
    @DisplayName("결제 포인트를 생성한다")
    void create() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        int amount = 10000;

        // when
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, amount);

        // then
        assertThat(paymentPoint.getMemberId()).isEqualTo(memberId);
        assertThat(paymentPoint.getPgOrderId()).isEqualTo(orderId);
        assertThat(paymentPoint.getAmount()).isEqualTo(amount);
        assertThat(paymentPoint.getStatus()).isEqualTo(PaymentPointStatus.REQUESTED);
    }

    @Test
    @DisplayName("결제를 승인 처리한다")
    void markConfirmed() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);

        String paymentMethod = "CARD";
        OffsetDateTime approvedAt = OffsetDateTime.now();
        String paymentKey = "test_payment_key";

        // when
        paymentPoint.markConfirmed(paymentMethod, approvedAt, paymentKey);

        // then
        assertThat(paymentPoint.getStatus()).isEqualTo(PaymentPointStatus.COMPLETED);
        assertThat(paymentPoint.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(paymentPoint.getApprovedAt()).isEqualTo(approvedAt);
        assertThat(paymentPoint.getPaymentKey()).isEqualTo(paymentKey);
    }

    @Test
    @DisplayName("결제를 실패 처리한다")
    void markFailed() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);

        String errorMessage = "카드 승인 실패";

        // when
        paymentPoint.markFailed(errorMessage);

        // then
        assertThat(paymentPoint.getStatus()).isEqualTo(PaymentPointStatus.FAILED);
    }

    @Test
    @DisplayName("결제를 환불 처리한다")
    void markRefunded() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);
        paymentPoint.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

        OffsetDateTime refundedAt = OffsetDateTime.now();
        String cancelReason = "고객 요청";

        // when
        paymentPoint.markRefunded(refundedAt, cancelReason);

        // then
        assertThat(paymentPoint.getStatus()).isEqualTo(PaymentPointStatus.REFUNDED);
        assertThat(paymentPoint.getRefundedAt()).isEqualTo(refundedAt);
        assertThat(paymentPoint.getRefundMessage()).isEqualTo(cancelReason);
    }
}
