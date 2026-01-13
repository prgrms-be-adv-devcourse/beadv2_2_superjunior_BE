package store._0982.point.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PgCancelCommand;
import store._0982.point.application.pg.PgCancelService;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.constant.PgPaymentStatus;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PgPaymentRepository;
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
class PgCancelServiceTest {

    private static final long REFUND_AMOUNT = 10000;

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @InjectMocks
    private PgCancelService pgCancelService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("포인트 환불을 성공적으로 처리한다")
    void refundPaymentPoint_success() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        pgPayment.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(REFUND_AMOUNT);

        TossPaymentResponse.CancelInfo cancelInfo = new TossPaymentResponse.CancelInfo(
                REFUND_AMOUNT,
                "고객 요청",
                OffsetDateTime.now()
        );

        TossPaymentResponse response = new TossPaymentResponse(
                "test_payment_key",
                orderId,
                REFUND_AMOUNT,
                "CARD",
                "CANCELED",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                List.of(cancelInfo)
        );

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
        when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.of(pointBalance));
        when(tossPaymentService.cancelPayment(any(), any())).thenReturn(response);

        // when
        pgCancelService.refundPaymentPoint(memberId, command);

        // then
        assertThat(pointBalance.getTotalBalance()).isZero();
        verify(tossPaymentService).cancelPayment(pgPayment, command);
    }

    @Test
    @DisplayName("존재하지 않는 주문으로 환불 시 예외가 발생한다")
    void refundPaymentPoint_fail_whenOrderNotFound() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("다른 회원의 주문을 환불하려고 하면 예외가 발생한다")
    void refundPaymentPoint_fail_whenOwnerMismatch() {
        // given
        UUID otherMemberId = UUID.randomUUID();
        PgPayment pgPayment = PgPayment.create(otherMemberId, orderId, 10000);

        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_OWNER_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("완료되지 않은 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenNotCompleted() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.NOT_COMPLETED_PAYMENT.getMessage());
    }

    @Test
    @DisplayName("이미 환불된 결제는 기존 정보를 반환한다")
    void refundPaymentPoint_alreadyRefunded() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        pgPayment.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");
        pgPayment.markRefunded(OffsetDateTime.now(), "이미 환불됨");

        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

        // when
        pgCancelService.refundPaymentPoint(memberId, command);

        // then
        assertThat(pgPayment.getStatus()).isEqualTo(PgPaymentStatus.REFUNDED);
        verify(tossPaymentService, never()).cancelPayment(any(), any());
    }

    @Test
    @DisplayName("환불 기간이 지난 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenRefundPeriodExpired() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        // 8일 전에 승인됨
        pgPayment.markConfirmed("CARD", OffsetDateTime.now().minusDays(8), "test_payment_key");

        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.REFUND_NOT_ALLOWED.getMessage());
    }

    @Test
    @DisplayName("회원 포인트가 없으면 환불할 수 없다")
    void refundPaymentPoint_fail_whenMemberPointNotFound() {
        // given
        PgPayment pgPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        pgPayment.markConfirmed("CARD", OffsetDateTime.now(), "test_payment_key");

        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
        when(pointBalanceRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
