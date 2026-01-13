package store._0982.point.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PgPaymentTest {

    @Test
    @DisplayName("결제 포인트를 생성한다")
    void create() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        int amount = 10000;

        // when
        PgPayment pgPayment = PgPayment.create(memberId, orderId, amount);

        // then
        assertThat(pgPayment.getMemberId()).isEqualTo(memberId);
        assertThat(pgPayment.getPgOrderId()).isEqualTo(orderId);
        assertThat(pgPayment.getAmount()).isEqualTo(amount);
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.PENDING);
    }

    @Test
    @DisplayName("결제를 승인 처리한다")
    void markConfirmed() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000);

        String paymentMethod = "CARD";
        OffsetDateTime approvedAt = OffsetDateTime.now();
        String paymentKey = "test_payment_key";

        // when
        pgPayment.markConfirmed(paymentMethod, approvedAt, paymentKey);

        // then
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.COMPLETED);
        assertThat(pgPayment.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(pgPayment.getApprovedAt()).isEqualTo(approvedAt);
        assertThat(pgPayment.getPaymentKey()).isEqualTo(paymentKey);
    }

    @Test
    @DisplayName("결제를 실패 처리한다")
    void markFailed() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000);

        String errorMessage = "카드 승인 실패";

        // when
        pgPayment.markFailed(errorMessage);

        // then
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.FAILED);
    }

    @Test
    @DisplayName("결제를 환불 처리한다")
    void markRefunded() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000);
        pgPayment.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

        OffsetDateTime refundedAt = OffsetDateTime.now();
        String cancelReason = "고객 요청";

        // when
        pgPayment.markRefunded(refundedAt, cancelReason);

        // then
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.REFUNDED);
        assertThat(pgPayment.getRefundedAt()).isEqualTo(refundedAt);
        assertThat(pgPayment.getRefundMessage()).isEqualTo(cancelReason);
    }
}
