package store._0982.batch.batch.grouppurchase.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.batch.domain.product.Product;
import store._0982.batch.domain.product.ProductRepository;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.batch.batch.grouppurchase.event.GroupPurchaseUpdatedEvent;

@Component
@RequiredArgsConstructor
public class GroupPurchaseUpdateListener {

    private final KafkaTemplate<String, GroupPurchaseEvent> kafkaTemplate;
    private final ProductRepository productRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOpened(GroupPurchaseUpdatedEvent event) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(event.groupPurchaseId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));
        Product product = productRepository.findById(groupPurchase.getProductId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        GroupPurchaseEvent kafkaEvent =
            groupPurchase.toEvent(
                    GroupPurchaseEvent.Status.valueOf(groupPurchase.getStatus().name()),
                    GroupPurchaseEvent.EventStatus.UPDATE_GROUP_PURCHASE,
                    product.getPrice(),
                    GroupPurchaseEvent.ProductCategory.valueOf(product.getCategory().name()));

        kafkaTemplate.send(
            KafkaTopics.GROUP_PURCHASE_CHANGED,
            kafkaEvent.getId().toString(),
            kafkaEvent
        );
    }
}
