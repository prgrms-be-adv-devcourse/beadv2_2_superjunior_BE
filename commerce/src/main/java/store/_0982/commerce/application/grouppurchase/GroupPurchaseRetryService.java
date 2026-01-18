package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPurchaseRetryService {

    private final GroupPurchaseRepository groupPurchaseRepository;

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 20)
    )
    public GroupPurchase participateWithRetry(UUID groupPurchaseId, int quantity) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (!groupPurchase.applyParticipationResult(true, quantity)) {
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_REACHED);
        }

        return groupPurchaseRepository.saveAndFlush(groupPurchase);
    }

    @Recover
    public GroupPurchase recover(OptimisticLockingFailureException e,
                                 UUID groupPurchaseId,
                                 int quantity) {
        throw new CustomException(CustomErrorCode.CONCURRENT_PARTICIPATION_CONFLICT);
    }
}