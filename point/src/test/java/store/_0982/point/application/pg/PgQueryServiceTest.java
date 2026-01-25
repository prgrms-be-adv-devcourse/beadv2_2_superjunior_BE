package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.domain.PaymentRules;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PgQueryServiceTest {

    private static final String SAMPLE_PURCHASE_NAME = "테스트 공구";
    private static final int REFUND_DAYS = 14;
    private static final String PAYMENT_KEY = "test_payment_key";
    private static final long AMOUNT = 10000;

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @Mock
    private PaymentRules paymentRules;

    @InjectMocks
    private PgQueryService pgQueryService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("findCompletablePayment 메서드는")
    class FindCompletablePaymentTest {

        @Test
        @DisplayName("PENDING 상태의 결제를 조회할 수 있다")
        void success() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            PgPayment result = pgQueryService.findCompletablePayment(orderId, memberId);

            assertThat(result).isEqualTo(pgPayment);
            assertThat(result.getMemberId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 예외가 발생한다")
        void fail_whenPaymentNotFound() {
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pgQueryService.findCompletablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("다른 회원의 결제 조회 시 예외가 발생한다")
        void fail_whenOwnerMismatch() {
            UUID otherMemberId = UUID.randomUUID();
            PgPayment pgPayment = PgPayment.create(otherMemberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findCompletablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_OWNER_MISMATCH.getMessage());
        }

        @Test
        @DisplayName("이미 완료된 결제 조회 시 예외가 발생한다")
        void fail_whenAlreadyCompleted() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findCompletablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.ALREADY_COMPLETED_PAYMENT.getMessage());
        }

        @Test
        @DisplayName("실패한 결제 조회 시 예외가 발생한다")
        void fail_whenAlreadyFailed() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markFailed(PAYMENT_KEY);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findCompletablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.ALREADY_COMPLETED_PAYMENT.getMessage());
        }

        @Test
        @DisplayName("환불된 결제 조회 시 예외가 발생한다")
        void fail_whenAlreadyRefunded() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
            pgPayment.markRefunded(OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findCompletablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.ALREADY_COMPLETED_PAYMENT.getMessage());
        }
    }

    @Nested
    @DisplayName("findFailablePayment 메서드는")
    class FindFailablePaymentTest {

        @Test
        @DisplayName("PENDING 상태의 결제를 조회할 수 있다")
        void success() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            PgPayment result = pgQueryService.findFailablePayment(orderId, memberId);

            assertThat(result).isEqualTo(pgPayment);
            assertThat(result.getMemberId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 예외가 발생한다")
        void fail_whenPaymentNotFound() {
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pgQueryService.findFailablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("다른 회원의 결제 조회 시 예외가 발생한다")
        void fail_whenOwnerMismatch() {
            UUID otherMemberId = UUID.randomUUID();
            PgPayment pgPayment = PgPayment.create(otherMemberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findFailablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_OWNER_MISMATCH.getMessage());
        }

        @Test
        @DisplayName("이미 완료된 결제는 실패 처리할 수 없다")
        void fail_whenAlreadyCompleted() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findFailablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.CANNOT_HANDLE_FAILURE.getMessage());
        }

        @Test
        @DisplayName("이미 실패한 결제는 다시 실패 처리할 수 없다")
        void fail_whenAlreadyFailed() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markFailed(PAYMENT_KEY);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findFailablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.CANNOT_HANDLE_FAILURE.getMessage());
        }

        @Test
        @DisplayName("환불된 결제는 실패 처리할 수 없다")
        void fail_whenAlreadyRefunded() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
            pgPayment.markRefunded(OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findFailablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.CANNOT_HANDLE_FAILURE.getMessage());
        }
    }

    @Nested
    @DisplayName("findRefundablePayment 메서드는")
    class FindRefundablePaymentTest {

        @Test
        @DisplayName("COMPLETED 상태의 결제를 조회할 수 있다")
        void success() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
            when(paymentRules.getRefundDays()).thenReturn(REFUND_DAYS);

            PgPayment result = pgQueryService.findRefundablePayment(orderId, memberId);

            assertThat(result).isEqualTo(pgPayment);
            assertThat(result.getMemberId()).isEqualTo(memberId);
        }

        @Test
        @DisplayName("PARTIALLY_REFUNDED 상태의 결제를 조회할 수 있다")
        void success_partiallyRefunded() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
            pgPayment.applyRefund(1000L, OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
            when(paymentRules.getRefundDays()).thenReturn(REFUND_DAYS);

            PgPayment result = pgQueryService.findRefundablePayment(orderId, memberId);

            assertThat(result).isEqualTo(pgPayment);
        }

        @Test
        @DisplayName("존재하지 않는 결제 조회 시 예외가 발생한다")
        void fail_whenPaymentNotFound() {
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pgQueryService.findRefundablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("다른 회원의 결제 조회 시 예외가 발생한다")
        void fail_whenOwnerMismatch() {
            UUID otherMemberId = UUID.randomUUID();
            PgPayment pgPayment = PgPayment.create(otherMemberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findRefundablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.PAYMENT_OWNER_MISMATCH.getMessage());
        }

        @Test
        @DisplayName("완료되지 않은 결제는 환불할 수 없다")
        void fail_whenNotCompleted() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findRefundablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.NOT_COMPLETED_PAYMENT.getMessage());
        }

        @Test
        @DisplayName("이미 전액 환불된 결제는 환불할 수 없다")
        void fail_whenAlreadyRefunded() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
            pgPayment.markRefunded(OffsetDateTime.now());
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findRefundablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.ALREADY_REFUNDED_PAYMENT.getMessage());
        }

        @Test
        @DisplayName("환불 기간이 지난 결제는 환불할 수 없다")
        void fail_whenRefundPeriodExpired() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now().minusDays(REFUND_DAYS + 1), PAYMENT_KEY);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
            when(paymentRules.getRefundDays()).thenReturn(REFUND_DAYS);

            assertThatThrownBy(() -> pgQueryService.findRefundablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.REFUND_NOT_ALLOWED.getMessage());
        }

        @Test
        @DisplayName("실패한 결제는 환불할 수 없다")
        void fail_whenFailed() {
            PgPayment pgPayment = PgPayment.create(memberId, orderId, AMOUNT, SAMPLE_PURCHASE_NAME);
            pgPayment.markFailed(PAYMENT_KEY);
            when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

            assertThatThrownBy(() -> pgQueryService.findRefundablePayment(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.NOT_COMPLETED_PAYMENT.getMessage());
        }
    }
}
