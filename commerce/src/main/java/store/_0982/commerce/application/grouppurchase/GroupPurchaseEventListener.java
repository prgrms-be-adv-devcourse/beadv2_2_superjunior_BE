package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.commerce.application.grouppurchase.event.*;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class GroupPurchaseEventListener {

    private final KafkaTemplate<String, GroupPurchaseEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreated(GroupPurchaseCreatedEvent event) {
        GroupPurchaseEvent kafkaEvent = event.groupPurchase().toEvent(
                GroupPurchaseEvent.Status.SCHEDULED,
                GroupPurchaseEvent.EventStatus.CREATE_GROUP_PURCHASE,
                event.product().getPrice(),
                GroupPurchaseEvent.ProductCategory.valueOf(
                        event.product().getCategory().name()
                )
        );

        kafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

        kafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeleted(GroupPurchaseDeletedEvent event) {
        GroupPurchaseEvent kafkaEvent = event.groupPurchase().toEvent(
                GroupPurchaseEvent.Status.valueOf(
                        event.groupPurchase().getStatus().name()
                ),
                GroupPurchaseEvent.EventStatus.DELETE_GROUP_PURCHASE,
                null,
                null
        );

        kafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                kafkaEvent.getId().toString(),
                kafkaEvent
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGroupPurchaseParticipated(GroupPurchaseParticipatedEvent event) {
        GroupPurchaseEvent searchEvent = event.groupPurchase().toEvent(
                GroupPurchaseEvent.Status.valueOf(
                        event.groupPurchase().getStatus().name()
                ),
                GroupPurchaseEvent.EventStatus.DELETE_GROUP_PURCHASE,
                event.product().getPrice(),
                GroupPurchaseEvent.ProductCategory.valueOf(
                        event.product().getCategory().name()
                )
        );
        kafkaTemplate.send(
                KafkaTopics.GROUP_PURCHASE_CHANGED,
                searchEvent.getId().toString(),
                searchEvent
        );
    }

    @Async("groupPurchaseAsyncExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void participantCountSyncToDb(GroupPurchaseCountSyncEvent event){
        try{
            GroupPurchase groupPurchase = groupPurchaseRepository.findById(event.groupPurchaseId())
                    .orElse(null);

            if(groupPurchase == null){
                log.warn("공동구매 찾을 수 없음 : groupPurchaseId = {}", event.groupPurchaseId());
                return;
            }

            groupPurchase.syncCurrentQuantity(event.newCount());
            groupPurchaseRepository.save(groupPurchase);
        }catch(Exception e){
            log.error("Participate DB 동기화 실패");
        }
    }
}
