package store._0982.point.application.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderConfirmedEvent;
import store._0982.point.application.bonus.BonusEarningService;
import store._0982.point.application.dto.bonus.BonusEarnCommand;
import store._0982.point.common.KafkaGroupIds;

@Component
@RequiredArgsConstructor
public class OrderConfirmedEventListener {

    private final BonusEarningService bonusEarningService;

    @RetryableTopic(exclude = {DuplicateKeyException.class, CustomException.class})
    @KafkaListener(
            topics = KafkaTopics.ORDER_CONFIRMED,
            groupId = KafkaGroupIds.PAYMENT_SERVICE,
            containerFactory = "baseEventListenerContainerFactory"
    )
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        BonusEarnCommand command = new BonusEarnCommand(
                event.getEventId(),
                event.getOrderId(),
                event.getGroupPurchaseId(),
                event.getProductCategory().name()
        );
        bonusEarningService.processBonus(event.getMemberId(), command);
    }
}
