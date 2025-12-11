package store._0982.elasticsearch.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.kafka.dto.SearchKafkaStatus;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentCommand;


@Component
@RequiredArgsConstructor
public class GroupPurchaseSearchEventConsumer {

    private final GroupPurchaseSearchService groupPurchaseSearchService;

    @RetryableTopic
    @KafkaListener(topics = KafkaTopics.GROUP_PURCHASE_ADDED, groupId = "search-service-group", containerFactory = "createGroupPurchaseKafkaListenerFactory")
    public void create(GroupPurchaseEvent event) {
        GroupPurchaseDocumentCommand command = GroupPurchaseDocumentCommand.from(event);
        groupPurchaseSearchService.saveGroupPurchaseDocument(command);
    }

    @RetryableTopic
    @KafkaListener(topics = KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED, groupId = "search-service-group", containerFactory = "changeGroupPurchaseKafkaListenerFactory")
    public void changed(GroupPurchaseEvent event) {
        if(event.getKafkaStatus().equals(SearchKafkaStatus.DELETE_GROUP_PURCHASE.name())){
            groupPurchaseSearchService.deleteGroupPurchaseDocument(event.getId());
        }else{
            GroupPurchaseDocumentCommand command = GroupPurchaseDocumentCommand.from(event);
            groupPurchaseSearchService.saveGroupPurchaseDocument(command);
        }
    }
}
