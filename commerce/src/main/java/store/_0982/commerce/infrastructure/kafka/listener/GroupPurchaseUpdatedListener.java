package store._0982.commerce.infrastructure.kafka.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.order.OrderService;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.log.ServiceLog;

@RequiredArgsConstructor
@Service
public class GroupPurchaseUpdatedListener {

    private final OrderService orderService;

    @ServiceLog
    @RetryableTopic(
            kafkaTemplate = "retryKafkaTemplate", exclude = CustomException.class
    )
    @KafkaListener(
            topics = KafkaTopics.GROUP_PURCHASE_CHANGED,
            groupId = "order-service-group",
            containerFactory = "orderListenerContainerFactory"
    )
    public void handleGroupPurchaseUpdatedEvent(GroupPurchaseEvent event){
        orderService.handleUpdatedGroupPurchase(event);
    }
}
