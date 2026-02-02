package store._0982.point.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.point.domain.constant.PaymentMethod;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PgPaymentCancelTest {

    @Test
    @DisplayName("PG 결제 취소 내역을 생성한다")
    void from() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000, "테스트 공구");
        pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), "test_payment_key");

        String cancelReason = "단순 변심";
        long cancelAmount = 10000;
        OffsetDateTime canceledAt = OffsetDateTime.now();
        String transactionKey = "test_transaction_key";

        // when
        PgPaymentCancel cancel = PgPaymentCancel.from(
                pgPayment,
                cancelReason,
                cancelAmount,
                canceledAt,
                transactionKey
        );

        // then
        assertThat(cancel.getPgPayment()).isEqualTo(pgPayment);
        assertThat(cancel.getPaymentKey()).isEqualTo("test_payment_key");
        assertThat(cancel.getCancelReason()).isEqualTo(cancelReason);
        assertThat(cancel.getCancelAmount()).isEqualTo(cancelAmount);
        assertThat(cancel.getCanceledAt()).isEqualTo(canceledAt);
        assertThat(cancel.getTransactionKey()).isEqualTo(transactionKey);
    }

    @Test
    @DisplayName("부분 취소 내역을 생성한다")
    void from_partialCancel() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000, "테스트 공구");
        pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), "test_payment_key");

        String cancelReason = "부분 취소";
        long cancelAmount = 3000;  // 부분 취소
        OffsetDateTime canceledAt = OffsetDateTime.now();
        String transactionKey = "test_transaction_key_partial";

        // when
        PgPaymentCancel cancel = PgPaymentCancel.from(
                pgPayment,
                cancelReason,
                cancelAmount,
                canceledAt,
                transactionKey
        );

        // then
        assertThat(cancel.getCancelAmount()).isEqualTo(3000);
        assertThat(cancel.getCancelReason()).isEqualTo(cancelReason);
    }

    @Test
    @DisplayName("여러 번 취소한 내역을 생성한다")
    void from_multipleCancels() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PgPayment pgPayment = PgPayment.create(memberId, orderId, 10000, "테스트 공구");
        pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), "test_payment_key");

        // when
        PgPaymentCancel cancel1 = PgPaymentCancel.from(
                pgPayment,
                "부분 취소 1",
                3000,
                OffsetDateTime.now(),
                "key1"
        );

        PgPaymentCancel cancel2 = PgPaymentCancel.from(
                pgPayment,
                "부분 취소 2",
                4000,
                OffsetDateTime.now(),
                "key2"
        );

        // then
        assertThat(cancel1.getCancelAmount()).isEqualTo(3000);
        assertThat(cancel2.getCancelAmount()).isEqualTo(4000);
        assertThat(cancel1.getPgPayment()).isEqualTo(pgPayment);
        assertThat(cancel2.getPgPayment()).isEqualTo(pgPayment);
    }
}
