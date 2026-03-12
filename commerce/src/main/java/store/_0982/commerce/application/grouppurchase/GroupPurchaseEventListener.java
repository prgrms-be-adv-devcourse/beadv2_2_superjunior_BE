package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseCreatedEvent;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseDeletedEvent;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseParticipatedEvent;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseUpdatedEvent;
import store._0982.commerce.infrastructure.outbox.OutboxEventService;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

@Component
@RequiredArgsConstructor
public class GroupPurchaseEventListener {

    private final OutboxEventService outboxEventService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCreated(GroupPurchaseCreatedEvent event) {
        GroupPurchaseEvent kafkaEvent = event.groupPurchase().toEvent(
                GroupPurchaseEvent.Status.SCHEDULED,
                GroupPurchaseEvent.EventStatus.CREATE_GROUP_PURCHASE,
                event.product().getPrice(),
                GroupPurchaseEvent.ProductCategory.valueOf(
                        event.product().getCategory().name()
                )
        );

        outboxEventService.record(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent,
                "GroupPurchase",
                kafkaEvent.getId().toString()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUpdated(GroupPurchaseUpdatedEvent event) {
        GroupPurchaseEvent kafkaEvent = event.groupPurchase().toEvent(
                GroupPurchaseEvent.Status.valueOf(
                        event.groupPurchase().getStatus().name()
                ),
                GroupPurchaseEvent.EventStatus.UPDATE_GROUP_PURCHASE,
                event.product().getPrice(),
                GroupPurchaseEvent.ProductCategory.valueOf(
                        event.product().getCategory().name()
                )
        );

        outboxEventService.record(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent,
                "GroupPurchase",
                kafkaEvent.getId().toString()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleDeleted(GroupPurchaseDeletedEvent event) {
        GroupPurchaseEvent kafkaEvent = event.groupPurchase().toEvent(
                GroupPurchaseEvent.Status.valueOf(
                        event.groupPurchase().getStatus().name()
                ),
                GroupPurchaseEvent.EventStatus.DELETE_GROUP_PURCHASE,
                null,
                null
        );

        outboxEventService.record(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getEventId().toString(),
                kafkaEvent,
                "GroupPurchase",
                kafkaEvent.getId().toString()
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleGroupPurchaseParticipated(GroupPurchaseParticipatedEvent event) {
        GroupPurchaseEvent searchEvent = event.groupPurchase().toEvent(
                GroupPurchaseEvent.Status.valueOf(
                        event.groupPurchase().getStatus().name()
                ),
                GroupPurchaseEvent.EventStatus.INCREASE_PARTICIPATE,
                event.product().getPrice(),
                GroupPurchaseEvent.ProductCategory.valueOf(
                        event.product().getCategory().name()
                )
        );

        outboxEventService.record(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                searchEvent.getEventId().toString(),
                searchEvent,
                "GroupPurchase",
                searchEvent.getId().toString()
        );
    }
}
