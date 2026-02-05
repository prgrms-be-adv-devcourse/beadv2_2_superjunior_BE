package store._0982.commerce.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import store._0982.commerce.application.order.event.OrderCreateProcessedEvent;
import store._0982.commerce.infrastructure.messaging.kafka.OrderCreatedEventMapper;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderCreatedEvent;

@Component
@RequiredArgsConstructor
public class OrderCreatedKafkaEventPublisher {

    private final KafkaTemplate<String, OrderCreatedEvent> orderCreatedEventKafkaTemplate;
    private final OrderCreatedEventMapper orderCreatedEventMapper;

    public void publish(OrderCreateProcessedEvent event){
        OrderCreatedEvent kafkaEvent = orderCreatedEventMapper.toMessage(
                event.order(),
                event.productName()
        );

        orderCreatedEventKafkaTemplate.send(
                KafkaTopics.ORDER_CREATED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent
        );
    }
}
