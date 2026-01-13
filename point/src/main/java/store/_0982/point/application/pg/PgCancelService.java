package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.PgCancelCommand;
import store._0982.point.client.dto.TossPaymentResponse;
import store._0982.point.domain.entity.PgPayment;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PgCancelService {

    private final TossPaymentService tossPaymentService;
    private final PgTransactionManager pgTransactionManager;

    @ServiceLog
    public void refundPaymentPoint(UUID memberId, PgCancelCommand command) {
        UUID orderId = command.orderId();
        PgPayment pgPayment = pgTransactionManager.markRefundPending(orderId, memberId);
        TossPaymentResponse response = tossPaymentService.cancelPayment(pgPayment, command);
        pgTransactionManager.markRefundedPayment(response, orderId, memberId);
    }
}
