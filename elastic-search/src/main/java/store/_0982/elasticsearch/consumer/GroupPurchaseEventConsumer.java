package store._0982.elasticsearch.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentCommand;


@Component
@RequiredArgsConstructor
public class GroupPurchaseEventConsumer {

    private final GroupPurchaseSearchService groupPurchaseSearchService;

    @KafkaListener(topics = KafkaTopics.GROUP_PURCHASE_ADDED, groupId = "search-service-group", containerFactory = "createGroupPurchaseKafkaListenerFactory")
    public void create(GroupPurchaseEvent event) {
        GroupPurchaseDocumentCommand command = GroupPurchaseDocumentCommand.from(event);
        groupPurchaseSearchService.saveGroupPurchaseDocument(command);
    }

//    @RetryableTopic
//    @KafkaListener(topics = KafkaTopics.PRODUCT_DELETED, groupId = "search-service-group", containerFactory = "deleteProductKafkaListenerFactory")
//    public void delete(UUID id) {
//        groupPurchaseSearchService.deleteGroupPurchaseDocument(id);
//    }
}
