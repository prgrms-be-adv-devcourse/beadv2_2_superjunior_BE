package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.PgConfirmCommand;
import store._0982.point.application.dto.PgCancelCommand;
import store._0982.point.client.OrderServiceClient;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgConfirmService {

    private final TossPaymentService tossPaymentService;
    private final PgTransactionManager pgTransactionManager;
    private final OrderServiceClient orderServiceClient;

    // TODO: 결제 승인에 대한 동시성 처리 필요
    @ServiceLog
    public void confirmPayment(PgConfirmCommand command, UUID memberId) {
        String paymentKey = command.paymentKey();
        PgPayment pgPayment = pgTransactionManager.findCompletablePayment(paymentKey, memberId);

        UUID orderId = command.orderId();
        OrderInfo order = orderServiceClient.getOrder(orderId, memberId);
        order.validateConfirmable(memberId, orderId, command.amount());

        TossPaymentResponse tossPaymentResponse = tossPaymentService.confirmPayment(pgPayment, command);

        // TODO: 여기서는 예외가 발생했다고 바로 롤백하기보다는 재시도 로직이 있는 게 좋을 것 같음
        try {
            pgTransactionManager.markConfirmedPayment(tossPaymentResponse, paymentKey, memberId);
        } catch (Exception e) {
            log.error("[Error] Failed to mark payment confirmed. Trying to rollback...", e);
            rollbackPayment(command, pgPayment, memberId);
        }
    }

    // 1차 방어선: 결제 성공 처리에 실패했을 때 토스 API에 취소 요청
    private void rollbackPayment(PgConfirmCommand command, PgPayment pgPayment, UUID memberId) {
        String cancelReason = "System Error";
        try {
            PgCancelCommand refundCommand = new PgCancelCommand(command.orderId(), cancelReason, command.amount());
            tossPaymentService.cancelPayment(pgPayment, refundCommand);
        } catch (Exception e) {
            // TODO: 취소 실패된 결제에 대한 배치 처리 추가
            log.error("[Service] Failed to rollback payment", e);
            throw new CustomException(CustomErrorCode.PAYMENT_PROCESS_FAILED_REFUNDED);
        }

        try {
            pgTransactionManager.markFailedPaymentBySystem(cancelReason, pgPayment.getPaymentKey(), memberId);
        } catch (Exception e) {
            // TODO: 돈은 환불됐는데 DB에는 아직 PENDING 중으로 남아있음 -> 재시도? 배치?
            log.error("[Error] Failed to mark payment failed", e);
            throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
