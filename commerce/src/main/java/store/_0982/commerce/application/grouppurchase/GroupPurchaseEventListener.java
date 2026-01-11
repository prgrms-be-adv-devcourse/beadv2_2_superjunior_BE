package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseCreatedEvent;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseDeletedEvent;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseParticipatedEvent;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseUpdatedEvent;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseChangedEvent;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

@Component
@RequiredArgsConstructor
public class GroupPurchaseEventListener {

    private final KafkaTemplate<String, GroupPurchaseEvent> upsertKafkaTemplate;
    private final KafkaTemplate<String, GroupPurchaseChangedEvent> notificationKafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(GroupPurchaseCreatedEvent event) {
        GroupPurchaseEvent kafkaEvent = event.groupPurchase().toEvent(
                event.sellerName(),
                GroupPurchaseEvent.SearchKafkaStatus.CREATE_GROUP_PURCHASE,
                event.product().toEvent()
        );

        upsertKafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CREATED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUpdated(GroupPurchaseUpdatedEvent event) {
        GroupPurchaseEvent kafkaEvent = event.groupPurchase().toEvent(
                event.sellerName(),
                GroupPurchaseEvent.SearchKafkaStatus.UPDATE_GROUP_PURCHASE,
                event.product().toEvent()
        );

        upsertKafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleted(GroupPurchaseDeletedEvent event) {
        GroupPurchaseEvent kafkaEvent = event.groupPurchase().toEvent(
                "",
                GroupPurchaseEvent.SearchKafkaStatus.DELETE_GROUP_PURCHASE,
                null
        );

        upsertKafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupPurchaseParticipated(GroupPurchaseParticipatedEvent event) {
        GroupPurchase groupPurchase = event.groupPurchase();

        // 공동구매 성공 시 알림 이벤트 발행
        if (groupPurchase.getStatus() == GroupPurchaseStatus.SUCCESS) {
            GroupPurchaseChangedEvent notificationEvent = groupPurchase.toChangedEvent(
                    GroupPurchaseChangedEvent.Status.SUCCESS,
                    (long) groupPurchase.getCurrentQuantity() * groupPurchase.getDiscountedPrice()
            );
            notificationKafkaTemplate.send(
                    KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED,
                    notificationEvent.getId().toString(),
                    notificationEvent
            );
        }

        // 검색 서비스용 Kafka 이벤트 발행
        GroupPurchaseEvent searchEvent = groupPurchase.toEvent(
                event.sellerName(),
                GroupPurchaseEvent.SearchKafkaStatus.INCREASE_PARTICIPATE,
                event.product().toEvent()
        );
        upsertKafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                searchEvent.getId().toString(),
                searchEvent
        );
    }
}
