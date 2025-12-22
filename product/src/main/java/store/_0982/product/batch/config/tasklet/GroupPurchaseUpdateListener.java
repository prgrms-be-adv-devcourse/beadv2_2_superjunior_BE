package store._0982.product.batch.config.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.product.batch.config.tasklet.event.GroupPurchaseUpdatedEvent;
import store._0982.product.client.MemberClient;
import store._0982.product.domain.grouppurchase.GroupPurchase;
import store._0982.product.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.product.domain.product.Product;
import store._0982.product.domain.product.ProductRepository;
import store._0982.product.exception.CustomErrorCode;

@Component
@RequiredArgsConstructor
public class GroupPurchaseUpdateListener {

    private final KafkaTemplate<String, GroupPurchaseEvent> kafkaTemplate;
    private final ProductRepository productRepository;
    private final MemberClient memberClient;
    private final GroupPurchaseRepository groupPurchaseRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOpened(GroupPurchaseUpdatedEvent event) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(event.groupPurchaseId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));
        Product product = productRepository.findById(groupPurchase.getProductId())
            .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        String sellerName =
            memberClient.getMember(product.getSellerId()).data().name();

        GroupPurchaseEvent kafkaEvent =
            groupPurchase.toEvent(sellerName, GroupPurchaseEvent.SearchKafkaStatus.UPDATE_GROUP_PURCHASE, product.toEvent());

        kafkaTemplate.send(
            KafkaTopics.GROUP_PURCHASE_CHANGED,
            kafkaEvent.getId().toString(),
            kafkaEvent
        );
    }
}
