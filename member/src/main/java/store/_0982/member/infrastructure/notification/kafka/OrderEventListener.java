package store._0982.member.infrastructure.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCreatedEvent;
import store._0982.member.application.notification.dto.OrderCompletedCommand;
import store._0982.member.application.notification.inapp.OrderInAppNotificationService;
import store._0982.member.common.notification.KafkaGroupIds;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final OrderInAppNotificationService orderInAppNotificationService;

    @RetryableTopic(exclude = CustomException.class)
    @KafkaListener(
            topics = KafkaTopics.ORDER_CREATED,
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppListenerContainerFactory"
    )
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        OrderCompletedCommand command = new OrderCompletedCommand(
                event.getId(),
                event.getMemberId(),
                event.getProductName()
        );
        orderInAppNotificationService.notifyOrderCompleted(command);
    }
}
