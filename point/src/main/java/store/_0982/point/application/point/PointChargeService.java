package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.PointChargeCommand;
import store._0982.point.application.dto.PointBalanceInfo;
import store._0982.point.domain.entity.PointBalance;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointChargeService {

    private final PointTxManager pointTxManager;

    @ServiceLog
    public PointBalanceInfo chargePoints(PointChargeCommand command, UUID memberId) {
        PointBalance point = pointTxManager.chargePoints(
                memberId,
                command.idempotencyKey(),
                command.amount()
        );

        return PointBalanceInfo.from(point);
    }
}
