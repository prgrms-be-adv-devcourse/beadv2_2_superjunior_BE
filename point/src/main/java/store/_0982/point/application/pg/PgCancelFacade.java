package store._0982.point.application.pg;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.TossPaymentService;
import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.entity.PgPayment;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PgCancelFacade {

    private final TossPaymentService tossPaymentService;
    private final PgQueryService pgQueryService;
    private final PgCancelService pgCancelService;

    @ServiceLog
    public void refundPayment(UUID memberId, PgCancelCommand command) {
        UUID orderId = command.orderId();
        PgPayment pgPayment = pgQueryService.findRefundablePayment(orderId, memberId);
        TossPaymentInfo response = tossPaymentService.cancelPayment(pgPayment, command);
        pgCancelService.markRefundedPayment(response, orderId, memberId);
    }
}
