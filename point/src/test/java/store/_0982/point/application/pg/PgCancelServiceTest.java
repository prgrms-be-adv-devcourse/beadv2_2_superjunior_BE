package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.common.exception.CustomException;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.PaymentRules;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentCancel;
import store._0982.point.domain.event.PaymentCanceledTxEvent;
import store._0982.point.domain.repository.PgPaymentCancelRepository;
import store._0982.point.domain.repository.PgPaymentRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgCancelServiceTest {

    private static final int REFUND_DAYS = 14;
    private static final long REFUND_AMOUNT = 10000;
    private static final String PAYMENT_KEY = "test_payment_key";

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @Mock
    private PgPaymentCancelRepository pgPaymentCancelRepository;

    @Mock
    private PaymentRules paymentRules;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PgCancelService pgCancelService;

    private UUID memberId;
    private UUID orderId;
    private PgPayment pgPayment;
    private TossPaymentInfo response;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        pgPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        pgPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);

        TossPaymentInfo.CancelInfo cancelInfo = TossPaymentInfo.CancelInfo.builder()
                .cancelAmount(REFUND_AMOUNT)
                .cancelReason("단순 변심")
                .canceledAt(OffsetDateTime.now())
                .transactionKey("test_transaction_key")
                .build();

        response = TossPaymentInfo.builder()
                .paymentKey("test-payment-key")
                .orderId(orderId)
                .amount(REFUND_AMOUNT)
                .method("카드")
                .status(TossPaymentInfo.Status.CANCELED)
                .requestedAt(OffsetDateTime.now())
                .approvedAt(OffsetDateTime.now())
                .cancels(List.of(cancelInfo))
                .build();
    }

    @Test
    @DisplayName("포인트 환불을 성공적으로 처리한다")
    void refundPayment_success() {
        // given
        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
        when(paymentRules.getRefundDays()).thenReturn(REFUND_DAYS);

        // when
        pgCancelService.markRefundedPayment(response, orderId, memberId);

        // then
        verify(pgPaymentRepository, times(2)).findByOrderId(orderId);
        verify(pgPaymentCancelRepository).saveAllAndFlush(any());
        verify(applicationEventPublisher).publishEvent(any(PaymentCanceledTxEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 환불 시 예외가 발생한다")
    void refundPayment_fail_whenOrderNotFound() {
        // given
        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pgCancelService.markRefundedPayment(response, orderId, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("다른 회원의 주문을 환불하려고 하면 예외가 발생한다")
    void refundPayment_fail_whenOwnerMismatch() {
        // given
        PgPayment otherPayment = PgPayment.create(UUID.randomUUID(), orderId, REFUND_AMOUNT);
        otherPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(otherPayment));

        // when & then
        assertThatThrownBy(() -> pgCancelService.markRefundedPayment(response, memberId, orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_OWNER_MISMATCH.getMessage());

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("완료되지 않은 결제는 환불할 수 없다")
    void refundPayment_fail_whenNotCompleted() {
        // given
        PgPayment pendingPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pendingPayment));

        // when & then
        assertThatThrownBy(() -> pgCancelService.markRefundedPayment(response, memberId, orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.NOT_COMPLETED_PAYMENT.getMessage());

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("이미 환불된 결제를 환불 요청할 경우 예외가 발생한다")
    void refundPayment_alreadyRefunded() {
        // given
        PgPayment refundedPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        refundedPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
        refundedPayment.markRefunded(OffsetDateTime.now());

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(refundedPayment));

        // when & then
        assertThatThrownBy(() -> pgCancelService.markRefundedPayment(response, memberId, orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.ALREADY_REFUNDED_PAYMENT.getMessage());

        verify(pgPaymentCancelRepository, never()).save(any(PgPaymentCancel.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("환불 기간이 지난 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenRefundPeriodExpired() {
        // given
        PgPayment expiredPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        expiredPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now().minusDays(REFUND_DAYS + 1), PAYMENT_KEY);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(expiredPayment));
        when(paymentRules.getRefundDays()).thenReturn(REFUND_DAYS);

        // when & then
        assertThatThrownBy(() -> pgCancelService.markRefundedPayment(response, memberId, orderId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.REFUND_NOT_ALLOWED.getMessage());

        verify(applicationEventPublisher, never()).publishEvent(any());
    }
}
