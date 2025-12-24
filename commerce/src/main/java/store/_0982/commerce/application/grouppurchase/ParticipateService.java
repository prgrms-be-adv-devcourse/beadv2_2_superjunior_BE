package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseChangedEvent;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.log.ServiceLog;
import store._0982.commerce.application.grouppurchase.dto.ParticipateInfo;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipateService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductRepository productRepository;

    private final KafkaTemplate<String, GroupPurchaseEvent> upsertKafkaTemplate;
    private final KafkaTemplate<String, GroupPurchaseChangedEvent> notificationKafkaTemplate;
    private final MemberClient memberClient;

    @ServiceLog
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 20)
    )
    @Transactional
    public ParticipateInfo participate(GroupPurchase groupPurchase, int quantity) {

        boolean success = groupPurchase.increaseQuantity(quantity);
        if (!success) {
            return ParticipateInfo.failure(
                    groupPurchase.getStatus().name(),
                    groupPurchase.getRemainingQuantity(),
                    "참여할 수 없는 상태입니다. (수량 또는 상태)"
            );
        }

        groupPurchaseRepository.saveAndFlush(groupPurchase);

        if (groupPurchase.getStatus() == GroupPurchaseStatus.SUCCESS) {
            GroupPurchaseChangedEvent notificationEvent = groupPurchase.toChangedEvent(GroupPurchaseChangedEvent.Status.SUCCESS, (long) groupPurchase.getCurrentQuantity() * groupPurchase.getDiscountedPrice());
            notificationKafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED, notificationEvent.getId().toString(), notificationEvent);
        }

//        search kafka
        Product product = productRepository.findById(groupPurchase.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));
        String sellerName = memberClient.getMember(product.getSellerId()).data().name();
        GroupPurchaseEvent event = groupPurchase.toEvent(sellerName, GroupPurchaseEvent.SearchKafkaStatus.INCREASE_PARTICIPATE, product.toEvent());
        upsertKafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_CHANGED, event.getId().toString(), event);

        return ParticipateInfo.success(
                groupPurchase.getStatus().name(),
                groupPurchase.getRemainingQuantity(),
                "공동구매 참여가 완료되었습니다."
        );
    }

    @Recover
    public ParticipateInfo recover(OptimisticLockingFailureException e, GroupPurchase groupPurchase, int quantity) {
        return ParticipateInfo.failure(
                groupPurchase.getStatus().name(),
                groupPurchase.getRemainingQuantity(),
                "현재 참여자가 많아 잠시 후 다시 시도해주세요."
        );
    }

}
