package store._0982.point.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.application.dto.PointRefundInfo;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.constant.PaymentPointStatus;
import store._0982.point.domain.entity.MemberPoint;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.repository.MemberPointRepository;
import store._0982.point.domain.repository.PaymentPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointRefundServiceTest {

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private PaymentPointRepository paymentPointRepository;

    @Mock
    private MemberPointRepository memberPointRepository;

    @InjectMocks
    private PointRefundService pointRefundService;

    @Test
    @DisplayName("포인트 환불을 성공적으로 처리한다")
    void refundPaymentPoint_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);
        paymentPoint.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

        PointRefundCommand command = new PointRefundCommand(orderId, "고객 요청");

        MemberPoint memberPoint = new MemberPoint(memberId);
        memberPoint.addPoints(10000);

        TossPaymentResponse.CancelInfo cancelInfo = new TossPaymentResponse.CancelInfo(
                10000,
                "고객 요청",
                OffsetDateTime.now()
        );

        TossPaymentResponse response = new TossPaymentResponse(
                "test_payment_key",
                orderId,
                10000,
                "CARD",
                "CANCELED",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of(cancelInfo)
        );

        when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));
        when(memberPointRepository.findById(memberId)).thenReturn(Optional.of(memberPoint));
        when(tossPaymentService.cancelPayment(any(), any())).thenReturn(response);

        // when
        PointRefundInfo result = pointRefundService.refundPaymentPoint(memberId, command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.status()).isEqualTo(PaymentPointStatus.REFUNDED);
        assertThat(memberPoint.getPointBalance()).isZero();
        verify(tossPaymentService).cancelPayment(paymentPoint, command);
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 환불 시 예외가 발생한다")
    void refundPaymentPoint_fail_whenOrderNotFound() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PointRefundCommand command = new PointRefundCommand(orderId, "고객 요청");

        when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointRefundService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("다른 회원의 주문을 환불하려고 하면 예외가 발생한다")
    void refundPaymentPoint_fail_whenOwnerMismatch() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID otherMemberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(otherMemberId, orderId, 10000);

        PointRefundCommand command = new PointRefundCommand(orderId, "고객 요청");

        when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));

        // when & then
        assertThatThrownBy(() -> pointRefundService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_OWNER_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("완료되지 않은 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenNotCompleted() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);
        // markConfirmed 호출하지 않음 (REQUESTED 상태)

        PointRefundCommand command = new PointRefundCommand(orderId, "고객 요청");

        when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));

        // when & then
        assertThatThrownBy(() -> pointRefundService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.NOT_COMPLETED_PAYMENT.getMessage());
    }

    @Test
    @DisplayName("이미 환불된 결제는 기존 정보를 반환한다")
    void refundPaymentPoint_alreadyRefunded() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);
        paymentPoint.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");
        paymentPoint.markRefunded(OffsetDateTime.now(), "이미 환불됨");

        PointRefundCommand command = new PointRefundCommand(orderId, "고객 요청");

        when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));

        // when
        PointRefundInfo result = pointRefundService.refundPaymentPoint(memberId, command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(PaymentPointStatus.REFUNDED);
        verify(tossPaymentService, never()).cancelPayment(any(), any());
    }

    @Test
    @DisplayName("환불 기간이 지난 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenRefundPeriodExpired() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);
        // 8일 전에 승인됨
        paymentPoint.markConfirmed("CARD", OffsetDateTime.now().minusDays(8), "test_payment_key");

        PointRefundCommand command = new PointRefundCommand(orderId, "고객 요청");

        when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));

        // when & then
        assertThatThrownBy(() -> pointRefundService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.REFUND_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("회원 포인트가 없으면 환불할 수 없다")
    void refundPaymentPoint_fail_whenMemberPointNotFound() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);
        paymentPoint.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

        PointRefundCommand command = new PointRefundCommand(orderId, "고객 요청");

        when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));
        when(memberPointRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointRefundService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
