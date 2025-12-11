package store._0982.product.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseSearchEvent;
import store._0982.common.kafka.dto.SearchKafkaStatus;
import store._0982.product.application.dto.ParticipateInfo;
import store._0982.product.client.MemberClient;
import store._0982.product.common.exception.CustomErrorCode;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.*;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipateService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductRepository productRepository;

    private final KafkaTemplate<String, GroupPurchaseSearchEvent> upsertKafkaTemplate;
    private final MemberClient memberClient;

    public GroupPurchase findGroupPurchaseById(UUID groupPurchaseId) {
        return groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));
    }

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
            // TODO : 판매자에게 공동구매 성공 알림 전송
        }

        //search kafka
        Product product = productRepository.findById(groupPurchase.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));
        String sellerName = memberClient.getMember(product.getSellerId()).data().name();
        GroupPurchaseSearchEvent event = groupPurchase.toEvent(product.getName(), sellerName, SearchKafkaStatus.INCREASE_PARTICIPATE);
        upsertKafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED, event.getId().toString(), event);

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
