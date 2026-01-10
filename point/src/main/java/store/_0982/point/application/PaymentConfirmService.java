package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PaymentConfirmCommand;
import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.Payment;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

    private final TossPaymentService tossPaymentService;
    private final PaymentTransactionManager paymentTransactionManager;
    private final OrderServiceClient orderServiceClient;

    // TODO: 결제 승인에 대한 동시성 처리 필요
    @ServiceLog
    public void confirmPayment(PaymentConfirmCommand command, UUID memberId) {
        String paymentKey = command.paymentKey();
        Payment payment = paymentTransactionManager.findCompletablePayment(paymentKey, memberId);

        UUID orderId = command.orderId();
        OrderInfo order = orderServiceClient.getOrder(orderId, memberId);
        order.validateConfirmable(memberId, orderId, command.amount());

        TossPaymentResponse tossPaymentResponse = tossPaymentService.confirmPayment(payment, command);
        try {
            paymentTransactionManager.markConfirmedPayment(tossPaymentResponse, paymentKey, memberId);
        } catch (Exception e) {
            log.error("[Error] Failed to mark payment confirmed. Trying to rollback...", e);
            rollbackPayment(command, payment);
        }
    }

    // 1차 방어선: 결제 성공 처리에 실패했을 때 토스 API에 취소 요청
    private void rollbackPayment(PaymentConfirmCommand command, Payment payment) {
        String cancelReason = "System Error";
        try {
            tossPaymentService.cancelPayment(payment, new PointRefundCommand(command.orderId(), cancelReason));
        } catch (Exception e) {
            // TODO: 취소 실패된 결제에 대한 배치 처리 추가
            log.error("[Service] Failed to rollback payment", e);
            throw new CustomException(CustomErrorCode.PAYMENT_PROCESS_FAILED_REFUNDED);
        }

        try {
            paymentTransactionManager.markFailedPayment(cancelReason, payment.getPaymentKey());
        } catch (Exception e) {
            // TODO: 돈은 환불됐는데 DB에는 아직 PENDING 중으로 남아있음 -> 재시도? 배치?
            log.error("[Error] Failed to mark payment failed", e);
            throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
