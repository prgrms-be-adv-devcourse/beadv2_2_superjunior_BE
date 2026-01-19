package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPurchaseDecreaseService {

    private final GroupPurchaseRepository groupPurchaseRepository;

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 10,
            backoff = @Backoff(
                    delay = 50,
                    multiplier = 2,
                    maxDelay = 500,
                    random = true
            )
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseQuantity(UUID groupPurchaseId, int quantity) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));
        groupPurchase.decreaseQuantity(quantity);
        groupPurchaseRepository.saveAndFlush(groupPurchase);
    }
}
