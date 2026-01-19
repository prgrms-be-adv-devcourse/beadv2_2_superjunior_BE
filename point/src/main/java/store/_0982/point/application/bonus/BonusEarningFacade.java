package store._0982.point.application.bonus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.point.application.OrderQueryService;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.client.dto.OrderInfo;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BonusEarningFacade {

    private final OrderQueryService orderQueryService;
    private final BonusEarningService bonusEarningService;

    // TODO: 구매 확정 이벤트가 추가되면 Kafka 리스너에서 호출하자
    public void earnBonusPoint(UUID memberId, BonusEarnCommand command) {
        OrderInfo orderInfo = orderQueryService.getOrderDetails(memberId, command.orderId());
        bonusEarningService.processBonus(memberId, command, orderInfo);
    }
}
