package store._0982.point.application.bonus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.point.application.OrderQueryService;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.client.dto.OrderInfo;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BonusFacade {

    private final OrderQueryService orderQueryService;
    private final BonusEarningService bonusEarningService;

    public void earnBonusPoint(UUID memberId, BonusEarnCommand command) {
        OrderInfo orderInfo = orderQueryService.getOrderDetails(memberId, command.orderId());
        bonusEarningService.processBonus(memberId, command, orderInfo);
    }
}
