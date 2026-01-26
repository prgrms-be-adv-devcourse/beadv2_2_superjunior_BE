package store._0982.batch.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseUpdateProcessedEvent;
import store._0982.batch.infrastructure.messaging.kafka.GroupPurchaseUpdateEventMapper;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

@Component
@RequiredArgsConstructor
public class GroupPurchaseUpdatedKafkaEventPublisher {
    private final KafkaTemplate<String, GroupPurchaseEvent> groupPurchaseEventKafkaTemplate;

    public void publish(GroupPurchaseUpdateProcessedEvent event){
        GroupPurchaseEvent kafkaEvent = GroupPurchaseUpdateEventMapper.toMessage(
                event.updateItem().groupPurchase(),
                event.updateItem().product()
        );

        groupPurchaseEventKafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent
        );
    }
}
