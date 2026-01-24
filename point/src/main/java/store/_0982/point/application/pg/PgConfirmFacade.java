package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.OrderQueryService;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.application.dto.pg.PgConfirmCommand;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.entity.PgPayment;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgConfirmFacade {

    private final TossPaymentService tossPaymentService;
    private final PgQueryService pgQueryService;
    private final OrderQueryService orderQueryService;
    private final PgConfirmService pgConfirmService;
    private final PgFailService pgFailService;

    @ServiceLog
    public void confirmPayment(PgConfirmCommand command, UUID memberId) {
        UUID orderId = command.orderId();
        PgPayment pgPayment = pgQueryService.findCompletablePayment(orderId, memberId);

        orderQueryService.validateOrderPayable(memberId, orderId, command.amount());

        TossPaymentInfo tossPaymentInfo = tossPaymentService.confirmPayment(pgPayment, command);
        try {
            pgConfirmService.markConfirmedPayment(tossPaymentInfo, orderId, memberId);
        } catch (Exception e) {
            log.error("[Error] Failed to mark payment confirmed. Trying to rollback...", e);
            rollbackPayment(command, pgPayment, memberId);
        }
    }

    private void rollbackPayment(PgConfirmCommand command, PgPayment pgPayment, UUID memberId) {
        String cancelReason = "System Error";
        try {
            PgCancelCommand refundCommand = new PgCancelCommand(command.orderId(), cancelReason, command.amount());
            tossPaymentService.cancelPayment(pgPayment, refundCommand);
        } catch (Exception e) {
            log.error("[Service] Failed to rollback payment", e);
            throw new CustomException(CustomErrorCode.PAYMENT_PROCESS_FAILED_REFUNDED);
        }

        try {
            pgFailService.markFailedPaymentBySystem(cancelReason, command.paymentKey(), command.orderId(), memberId);
        } catch (Exception e) {
            log.error("[Error] Failed to mark payment failed", e);
            throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
