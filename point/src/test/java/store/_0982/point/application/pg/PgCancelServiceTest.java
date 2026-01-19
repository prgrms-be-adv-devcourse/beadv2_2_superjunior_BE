package store._0982.point.application.pg;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.PaymentRules;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.entity.PgPaymentCancel;
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

    private static final long REFUND_AMOUNT = 10000;
    private static final String PAYMENT_KEY = "test_payment_key";

    @Mock
    private TossPaymentService tossPaymentService;

    @Mock
    private PgPaymentRepository pgPaymentRepository;

    @Mock
    private PgPaymentCancelRepository pgPaymentCancelRepository;

    @Spy
    private PaymentRules paymentRules;

    @InjectMocks
    private PgTxManager pgTxManager;
    private PgCancelService pgCancelService;

    private UUID memberId;
    private UUID orderId;
    private PgPayment pgPayment;
    private TossPaymentInfo response;

    @BeforeEach
    void setUp() {
        // 단위 테스트이므로 설정값이 자동으로 주입되지 않음. 실제 값 설정.
        paymentRules.setRefundDays(14);

        pgCancelService = new PgCancelService(tossPaymentService, pgTxManager);

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
    void refundPaymentPoint_success() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pgPayment));
        when(tossPaymentService.cancelPayment(pgPayment, command)).thenReturn(response);

        // when
        pgCancelService.refundPaymentPoint(memberId, command);

        // then
        verify(pgPaymentRepository, times(2)).findByOrderId(orderId);
        verify(tossPaymentService).cancelPayment(pgPayment, command);
        verify(pgPaymentCancelRepository).save(any(PgPaymentCancel.class));
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
                .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("다른 회원의 주문을 환불하려고 하면 예외가 발생한다")
    void refundPaymentPoint_fail_whenOwnerMismatch() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);
        // 다른 memberId로 생성
        PgPayment otherPayment = PgPayment.create(UUID.randomUUID(), orderId, REFUND_AMOUNT);
        otherPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
        
        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(otherPayment));

        // when & then
        // 실제 validateOwner() 로직이 수행되어 에러가 발생함
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_OWNER_MISMATCH.getMessage());
    }

    @Test
    @DisplayName("완료되지 않은 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenNotCompleted() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);
        // markConfirmed를 호출하지 않아 PENDING 상태인 결제
        PgPayment pendingPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(pendingPayment));

        // when & then
        // 실제 status 체크 로직이 수행됨
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.NOT_COMPLETED_PAYMENT.getMessage());
    }

    @Test
    @DisplayName("이미 환불된 결제를 환불 요청할 경우 예외가 발생한다")
    void refundPaymentPoint_alreadyRefunded() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);
        PgPayment refundedPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        refundedPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now(), PAYMENT_KEY);
        refundedPayment.markRefunded(OffsetDateTime.now());

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(refundedPayment));

        // when & then
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.ALREADY_REFUNDED_PAYMENT.getMessage());

        verify(tossPaymentService, never()).cancelPayment(refundedPayment, command);
        verify(pgPaymentCancelRepository, never()).save(any(PgPaymentCancel.class));
    }

    @Test
    @DisplayName("환불 기간이 지난 결제는 환불할 수 없다")
    void refundPaymentPoint_fail_whenRefundPeriodExpired() {
        // given
        PgCancelCommand command = new PgCancelCommand(orderId, "고객 요청", REFUND_AMOUNT);
        
        // 15일 전에 승인된 실제 결제 객체 생성 (환불 기간 14일 초과)
        PgPayment expiredPayment = PgPayment.create(memberId, orderId, REFUND_AMOUNT);
        expiredPayment.markConfirmed(PaymentMethod.CARD, OffsetDateTime.now().minusDays(15), PAYMENT_KEY);

        when(pgPaymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(expiredPayment));

        // when & then
        // Mocking(doThrow) 없이 실제 validateRefundTerms() 로직이 수행되어 날짜 차이를 계산함
        assertThatThrownBy(() -> pgCancelService.refundPaymentPoint(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.REFUND_NOT_ALLOWED.getMessage());
    }
}
