package store._0982.product.application;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.product.application.dto.ParticipateInfo;
import store._0982.product.common.exception.CustomErrorCode;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;
import store._0982.product.domain.GroupPurchaseStatus;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class ParticipateService {

    private final GroupPurchaseRepository groupPurchaseRepository;

    @Retryable(
            retryFor = {OptimisticLockException.class, ObjectOptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 20)
    )
    @Transactional
    public ParticipateInfo participate(UUID groupPurchaseId, int quantity) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (!groupPurchase.canParticipate(quantity)) {
            return ParticipateInfo.failure(
                    groupPurchase.getStatus().name(),
                    groupPurchase.getRemainingQuantity(),
                    "참여할 수 없는 상태입니다. (수량 또는 상태)"
            );
        }

        boolean success = groupPurchase.increaseQuantity(quantity);
        if (!success) {
            return ParticipateInfo.failure(
                    groupPurchase.getStatus().name(),
                    groupPurchase.getRemainingQuantity(),
                    "남은 수량이 부족합니다."
            );
        }

        groupPurchaseRepository.save(groupPurchase);

         if (groupPurchase.getStatus() == GroupPurchaseStatus.SUCCESS) {
             // TODO : 판매자에게 공동구매 성공 알림 전송
         }

        return ParticipateInfo.success(
                groupPurchase.getStatus().name(),
                groupPurchase.getRemainingQuantity(),
                "공동구매 참여가 완료되었습니다."
        );
    }

    @Recover
    public ParticipateInfo recoverOptimisticLock(OptimisticLockException e, UUID groupPurchaseId) {
        GroupPurchase gp = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        return ParticipateInfo.failure(
                gp.getStatus().name(),
                gp.getRemainingQuantity(),
                "현재 참여자가 많아 잠시 후 다시 시도해주세요."
        );
    }

}
