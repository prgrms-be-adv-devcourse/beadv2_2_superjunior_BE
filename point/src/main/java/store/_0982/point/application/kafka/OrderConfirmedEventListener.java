package store._0982.point.application.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderConfirmedEvent;
import store._0982.point.application.OrderQueryService;
import store._0982.point.application.bonus.BonusEarningService;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.client.dto.OrderInfo;
import store._0982.point.common.KafkaGroupIds;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderConfirmedEventListener {

    private final BonusEarningService bonusEarningService;
    private final OrderQueryService orderQueryService;

    @RetryableTopic(exclude = CustomException.class)
    @KafkaListener(
            topics = KafkaTopics.ORDER_CONFIRMED,
            groupId = KafkaGroupIds.PAYMENT_SERVICE,
            containerFactory = "orderConfirmedEventListenerContainerFactory"
    )
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        UUID memberId = event.getMemberId();
        UUID orderId = event.getOrderId();
        OrderInfo orderInfo = orderQueryService.getOrderDetails(memberId, orderId);

        BonusEarnCommand command = new BonusEarnCommand(event.getEventId(), orderId);
        bonusEarningService.processBonus(memberId, command, orderInfo);
    }
}
