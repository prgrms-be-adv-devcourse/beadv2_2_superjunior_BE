package store._0982.commerce.integration.order.testsupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 재시도 로직을 담당하는 서비스
 *
 * @Retryable이 적용되어 실패 시 자동으로 재시도합니다.
 */
@Service
public class RetryQuantityService {

    @Autowired
    private DecreaseQuantityTxService transactionalQuantityService;

    private final AtomicInteger attemptCount = new AtomicInteger(0);

    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 4,
        backoff = @Backoff(delay = 10)
    )
    public void decreaseQuantity(UUID groupPurchaseId, int quantity) {
        int currentAttempt = attemptCount.incrementAndGet();
        System.out.println("! decreaseQuantity 시도 #" + currentAttempt);

        // 별도 클래스의 @Transactional 메서드 호출
        transactionalQuantityService.decreaseQuantityTx(currentAttempt, groupPurchaseId);
    }

    public AtomicInteger getAttemptCount() {
        return attemptCount;
    }

    public void resetAttemptCount() {
        attemptCount.set(0);
    }
}
