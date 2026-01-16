package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.point.application.dto.point.PointReturnCommand;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointReturnService {

    private final PointTxManager pointTxManager;

    @ServiceLog
    public void returnPoints(UUID memberId, PointReturnCommand command) {
        pointTxManager.returnPoints(
                memberId,
                command.orderId(),
                command.idempotencyKey(),
                command.amount(),
                command.cancelReason()
        );
    }
}
