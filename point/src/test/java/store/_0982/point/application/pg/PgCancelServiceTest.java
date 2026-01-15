package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.PgCancelCommand;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PgCancelServiceTest {

    private static final long REFUND_AMOUNT = 10000;
    private static final String PAYMENT_KEY = "test_payment_key";

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private PgTxManager pgTxManager;

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
    void refundPaymentPoint_success() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgTxManager.markRefundPending(orderId, memberId)).thenReturn(pgPayment);
        when(tossPaymentService.cancelPayment(pgPayment, command)).thenReturn(response);
        doNothing().when(pgTxManager).markRefundedPayment(response, orderId, memberId);

        // when
        pgCancelService.refundPaymentPoint(memberId, command);

        // then
        verify(pgTxManager).markRefundPending(orderId, memberId);
        verify(tossPaymentService).cancelPayment(pgPayment, command);
        verify(pgTxManager).markRefundedPayment(response, orderId, memberId);
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 환불 시 예외가 발생한다")
    void refundPaymentPoint_fail_whenOrderNotFound() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgTxManager.markRefundPending(orderId, memberId))
                .thenThrow(new CustomException(CustomErrorCode.PAYMENT_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("다른 회원의 주문을 환불하려고 하면 예외가 발생한다")
    void refundPaymentPoint_fail_whenOwnerMismatch() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgTxManager.markRefundPending(orderId, memberId))
                .thenThrow(new CustomException(CustomErrorCode.PAYMENT_OWNER_MISMATCH));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_OWNER_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("완료되지 않은 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenNotCompleted() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgTxManager.markRefundPending(orderId, memberId))
                .thenThrow(new CustomException(CustomErrorCode.NOT_COMPLETED_PAYMENT));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.NOT_COMPLETED_PAYMENT.getMessage());
    }

    @Test
    @DisplayName("이미 환불된 결제는 기존 정보를 반환한다")
    void refundPaymentPoint_alreadyRefunded() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);
        PgPayment refundedPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        refundedPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
        refundedPayment.markRefunded(OffsetDateTime.now());

        when(pgTxManager.markRefundPending(orderId, memberId)).thenReturn(refundedPayment);
        when(tossPaymentService.cancelPayment(refundedPayment, command)).thenReturn(response);
        doNothing().when(pgTxManager).markRefundedPayment(response, orderId, memberId);

        // when
        pgCancelService.refundPaymentPoint(memberId, command);

        // then
        verify(tossPaymentService).cancelPayment(refundedPayment, command);
    }

    @Test
    @DisplayName("환불 기간이 지난 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenRefundPeriodExpired() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgTxManager.markRefundPending(orderId, memberId))
                .thenThrow(new CustomException(CustomErrorCode.REFUND_NOT_ALLOWED));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.REFUND_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("회원 포인트가 없으면 환불할 수 없다")
    void refundPaymentPoint_fail_whenMemberPointNotFound() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgTxManager.markRefundPending(orderId, memberId))
                .thenThrow(new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
