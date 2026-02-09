package store._0982.batch.infrastructure.kafka.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.dto.GroupPurchaseResultProjection;
import store._0982.batch.infrastructure.messaging.kafka.GroupPurchaseFailedEventMapper;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseFailedEvent;

@Component
@RequiredArgsConstructor
public class GroupPurchaseFailedKafkaEventPublisher {

    private final KafkaTemplate<String, GroupPurchaseFailedEvent> groupPurchaseFailedEventKafkaTemplate;

    public void publish(GroupPurchaseResultProjection item) {
        GroupPurchaseFailedEvent kafkaEvent = GroupPurchaseFailedEventMapper.toMessage(item);

        groupPurchaseFailedEventKafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_FAILED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent
        );
    }
}
