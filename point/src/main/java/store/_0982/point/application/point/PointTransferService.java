package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointTransferCommand;
import store._0982.point.domain.entity.PointBalance;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointTransferService {

    private final PointTxManager pointTxManager;

    @ServiceLog
    public PointBalanceInfo transfer(UUID memberId, PointTransferCommand command) {
        PointBalance balance = pointTxManager.transfer(
                memberId,
                command.idempotencyKey(),
                command.amount()
        );

        return PointBalanceInfo.from(balance);
    }
}
