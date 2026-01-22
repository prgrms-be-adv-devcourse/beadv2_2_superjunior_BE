package store._0982.commerce.infrastructure.kafka.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.order.OrderService;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseFailedEvent;

@RequiredArgsConstructor
@Service
public class GroupPurchaseFailedListener {

    private final OrderService orderService;

    @Transactional
    @RetryableTopic(
        kafkaTemplate = "retryKafkaTemplate", exclude = CustomException.class
    )
    @KafkaListener(
            topics = KafkaTopics.GROUP_PURCHASE_FAILED,
            groupId = "order-service-group",
            containerFactory = "orderListenerContainerFactory"
    )
    public void handleGroupPurchaseFailedEvent(GroupPurchaseFailedEvent event){
        orderService.processGroupPurchaseFailure(event.getGroupPurchaseId());
    }
}
