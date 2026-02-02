package store._0982.point.application.point;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.point.application.OrderQueryService;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointDeductCommand;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PointDeductFacade {

    private final PointDeductService pointDeductService;
    private final OrderQueryService orderQueryService;

    public PointBalanceInfo deductPoints(UUID memberId, PointDeductCommand command) {
        orderQueryService.validateOrderPayable(memberId, command.orderId(), command.amount());
        return pointDeductService.processDeductionWithBonus(memberId, command);
    }
}
