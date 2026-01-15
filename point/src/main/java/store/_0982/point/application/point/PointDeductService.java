package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.OrderValidator;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointInfo;
import store._0982.point.domain.entity.PointBalance;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointDeductService {

    private final PointTxManager pointTxManager;
    private final OrderValidator orderValidator;

    @ServiceLog
    public PointInfo deductPoints(UUID memberId, PointDeductCommand command) {
        UUID idempotencyKey = command.idempotencyKey();
        UUID orderId = command.orderId();
        long amount = command.amount();

        orderValidator.validateOrderPayable(memberId, orderId, amount);

        PointBalance point = pointTxManager.deductPoints(
                memberId,
                orderId,
                idempotencyKey,
                amount
        );

        return PointInfo.from(point);
    }
}
