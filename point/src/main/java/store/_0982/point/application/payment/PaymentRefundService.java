package store._0982.point.application.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.Payment;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentRefundService {

    private final TossPaymentService tossPaymentService;
    private final PaymentTransactionManager paymentTransactionManager;

    @ServiceLog
    public void refundPaymentPoint(UUID memberId, PointRefundCommand command) {
        UUID orderId = command.orderId();
        Payment payment = paymentTransactionManager.markRefundPending(orderId, memberId);
        TossPaymentResponse response = tossPaymentService.cancelPayment(payment, command);
        paymentTransactionManager.markRefundedPayment(response, orderId, memberId);
    }
}
