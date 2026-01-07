package store._0982.point.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PointChargeFailCommand;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.entity.PaymentPointFailure;
import store._0982.point.domain.repository.PaymentPointFailureRepository;
import store._0982.point.domain.repository.PaymentPointRepository;
import store._0982.point.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentPointFailureServiceTest {

    @Mock
    private PaymentPointRepository paymentPointRepository;

    @Mock
    private PaymentPointFailureRepository paymentPointFailureRepository;

    @InjectMocks
    private PaymentPointFailureService paymentPointFailureService;

    private UUID memberId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
    }

    @Test
    @DisplayName("결제 실패 정보를 저장한다")
    void handlePaymentFailure_success() {
        // given
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000L);

        PointChargeFailCommand command = new PointChargeFailCommand(
                orderId,
                "test_payment_key",
                "PAYMENT_FAILED",
                "카드 승인 실패",
                10000L,
                "{}"
        );

        PaymentPointFailure failure = PaymentPointFailure.from(paymentPoint, command);

        when(paymentPointRepository.findByOrderIdWithLock(orderId)).thenReturn(Optional.of(paymentPoint));
        when(paymentPointFailureRepository.save(any(PaymentPointFailure.class))).thenReturn(failure);

        // when
        paymentPointFailureService.handlePaymentFailure(command, memberId);

        // then
        verify(paymentPointFailureRepository).save(any(PaymentPointFailure.class));
    }

    @Test
    @DisplayName("이미 실패 처리된 결제는 멱등성을 보장하며 실패 이력을 저장하지 않는다")
    void handlePaymentFailure_idempotency() {
        // given
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000L);
        paymentPoint.markFailed("이전 실패 사유");

        PointChargeFailCommand command = new PointChargeFailCommand(
                orderId,
                "test_payment_key",
                "PAYMENT_FAILED",
                "중복된 실패 요청",
                10000L,
                "{}"
        );

        when(paymentPointRepository.findByOrderIdWithLock(orderId)).thenReturn(Optional.of(paymentPoint));

        // when
        paymentPointFailureService.handlePaymentFailure(command, memberId);

        // then
        verify(paymentPointFailureRepository, never()).save(any(PaymentPointFailure.class));
    }

    @Test
    @DisplayName("이미 성공(COMPLETED)된 결제를 실패 처리하려 하면 예외가 발생한다")
    void handlePaymentFailure_exception_when_completed() {
        // given
        String paymentKey = "test_payment_key";
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000L);
        paymentPoint.markConfirmed("간편 결제", OffsetDateTime.now(), paymentKey);

        PointChargeFailCommand command = new PointChargeFailCommand(
                orderId,
                paymentKey,
                "PAYMENT_FAILED",
                "카드 승인 실패",
                10000L,
                "{}"
        );

        when(paymentPointRepository.findByOrderIdWithLock(orderId)).thenReturn(Optional.of(paymentPoint));

        // when & then
        assertThatThrownBy(() -> paymentPointFailureService.handlePaymentFailure(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.CANNOT_HANDLE_FAILURE.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 주문 ID로 실패 처리 요청 시 예외가 발생한다")
    void handlePaymentFailure_not_found() {
        // given
        PointChargeFailCommand command = new PointChargeFailCommand(
                orderId,
                "test_payment_key",
                "PAYMENT_FAILED",
                "실패",
                10000L,
                "{}"
        );

        when(paymentPointRepository.findByOrderIdWithLock(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentPointFailureService.handlePaymentFailure(command, memberId))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }
}
