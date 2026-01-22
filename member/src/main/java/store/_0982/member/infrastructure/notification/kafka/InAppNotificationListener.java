package store._0982.member.infrastructure.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCanceledEvent;
import store._0982.common.kafka.dto.OrderConfirmedEvent;
import store._0982.common.kafka.dto.OrderCreatedEvent;
import store._0982.member.application.notification.dispatch.InAppDispatchService;
import store._0982.member.application.notification.dto.OrderCanceledCommand;
import store._0982.member.application.notification.dto.OrderCompletedCommand;
import store._0982.member.application.notification.dto.OrderConfirmedCommand;
import store._0982.member.common.notification.CustomRetryableTopic;
import store._0982.member.common.notification.InAppKafkaListener;

@Component
@RequiredArgsConstructor
public class InAppNotificationListener {

    private final InAppDispatchService inAppDispatchService;

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.ORDER_CREATED)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        inAppDispatchService.notifyToInApp(OrderCompletedCommand.from(event));
    }

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.ORDER_CANCELED)
    public void handleOrderCanceledEvent(OrderCanceledEvent event) {
        inAppDispatchService.notifyToInApp(OrderCanceledCommand.from(event));
    }

    @CustomRetryableTopic
    @InAppKafkaListener(KafkaTopics.ORDER_CONFIRMED)
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        inAppDispatchService.notifyToInApp(OrderConfirmedCommand.from(event));
    }
}
