package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseChangedEvent;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.log.ServiceLog;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipateService {

    private final ProductRepository productRepository;
    private final GroupPurchaseRetryService groupPurchaseRetryService;

    private final KafkaTemplate<String, GroupPurchaseEvent> upsertKafkaTemplate;
    private final KafkaTemplate<String, GroupPurchaseChangedEvent> notificationKafkaTemplate;

    @ServiceLog
    @Transactional
    public void participate(UUID groupPurchaseId, int quantity, String sellerName) {
        GroupPurchase savedGroupPurchase = groupPurchaseRetryService.participateWithRetry(groupPurchaseId, quantity);

        // 공동구매 성공 시 알림 이벤트 발행
        if (savedGroupPurchase.getStatus() == GroupPurchaseStatus.SUCCESS) {
            GroupPurchaseChangedEvent notificationEvent = savedGroupPurchase.toChangedEvent(
                    GroupPurchaseChangedEvent.Status.SUCCESS,
                    (long) savedGroupPurchase.getCurrentQuantity() * savedGroupPurchase.getDiscountedPrice()
            );
            notificationKafkaTemplate.send(
                    KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED,
                    notificationEvent.getId().toString(),
                    notificationEvent
            );
        }

        // 검색 서비스용 Kafka 이벤트 발행
        Product product = productRepository.findById(savedGroupPurchase.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));
        GroupPurchaseEvent event = savedGroupPurchase.toEvent(
                sellerName,
                GroupPurchaseEvent.SearchKafkaStatus.INCREASE_PARTICIPATE,
                product.toEvent()
        );
        upsertKafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_CHANGED, event.getId().toString(), event);

    }

}
