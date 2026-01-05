package store._0982.point.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.point.application.dto.PointChargeFailCommand;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.entity.PaymentPointFailure;
import store._0982.point.domain.repository.PaymentPointFailureRepository;
import store._0982.point.domain.repository.PaymentPointRepository;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentPointFailureServiceTest {

    @Mock
    private PaymentPointRepository paymentPointRepository;

    @Mock
    private PaymentPointFailureRepository paymentPointFailureRepository;

    @InjectMocks
    private PaymentPointFailureService paymentPointFailureService;

    @Test
    @DisplayName("결제 실패 정보를 저장한다")
    void handlePaymentFailure_success() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        PaymentPoint paymentPoint = PaymentPoint.create(memberId, orderId, 10000);

        PointChargeFailCommand command = new PointChargeFailCommand(
                orderId,
                "test_payment_key",
                "PAYMENT_FAILED",
                "카드 승인 실패",
                10000L,
                "{}"
        );

        PaymentPointFailure failure = PaymentPointFailure.from(paymentPoint, command);

        when(paymentPointRepository.findByOrderId(orderId)).thenReturn(Optional.of(paymentPoint));
        when(paymentPointFailureRepository.save(any(PaymentPointFailure.class))).thenReturn(failure);

        // when
        paymentPointFailureService.handlePaymentFailure(command);

        // then
        verify(paymentPointFailureRepository).save(any(PaymentPointFailure.class));
    }
}
