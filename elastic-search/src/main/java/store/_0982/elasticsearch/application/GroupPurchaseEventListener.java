package store._0982.elasticsearch.application;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentCommand;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentInfo;
import store._0982.elasticsearch.infrastructure.GroupPurchaseRepository;


@Service
@RequiredArgsConstructor
public class GroupPurchaseEventListener {

    private final GroupPurchaseRepository groupPurchaseRepository;

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.GROUP_PURCHASE_CREATED, groupId = "search-service-group", containerFactory = "createGroupPurchaseKafkaListenerFactory")
    public void create(GroupPurchaseEvent event) {
        saveGroupPurchaseDocument(GroupPurchaseDocumentCommand.from(event));
    }

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.GROUP_PURCHASE_CHANGED, groupId = "search-service-group", containerFactory = "changeGroupPurchaseKafkaListenerFactory")
    public void changed(GroupPurchaseEvent event) {
        if(event.getKafkaStatus() == GroupPurchaseEvent.SearchKafkaStatus.DELETE_GROUP_PURCHASE){
                groupPurchaseRepository.deleteById(event.getId().toString());
        }else{
            saveGroupPurchaseDocument(GroupPurchaseDocumentCommand.from(event));
        }
    }

    public void saveGroupPurchaseDocument(GroupPurchaseDocumentCommand command) {
        GroupPurchaseDocumentInfo.from(groupPurchaseRepository.save(command.toDocument()));
    }
}
