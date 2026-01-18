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
import store._0982.elasticsearch.exception.ElasticsearchExecutor;
import store._0982.elasticsearch.infrastructure.GroupPurchaseRepository;
import store._0982.elasticsearch.infrastructure.product.ProductVectorRepository;


@Service
@RequiredArgsConstructor
public class GroupPurchaseEventListener {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ElasticsearchExecutor elasticsearchExecutor;
    private final ProductVectorRepository productVectorRepository;

    @RetryableTopic
    @ServiceLog
    @KafkaListener(topics = KafkaTopics.GROUP_PURCHASE_CHANGED, groupId = "search-service-group", containerFactory = "changeGroupPurchaseKafkaListenerFactory")
    public void changed(GroupPurchaseEvent event) {
        if (event.getKafkaStatus() == GroupPurchaseEvent.EventStatus.DELETE_GROUP_PURCHASE) {
            elasticsearchExecutor.execute(() -> groupPurchaseRepository.deleteById(event.getId().toString()));
            return;
        }
        float[] productVector = null;
        if (event.getProductId() != null) {
            productVector = productVectorRepository.findVectorByProductId(event.getProductId());
        }
        saveGroupPurchaseDocument(GroupPurchaseDocumentCommand.from(event, productVector));
    }

    public void saveGroupPurchaseDocument(GroupPurchaseDocumentCommand command) {
        elasticsearchExecutor.execute(
                () -> GroupPurchaseDocumentInfo.from(groupPurchaseRepository.save(command.toDocument()))
        );
    }
}
