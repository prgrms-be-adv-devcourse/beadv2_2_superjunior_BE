package store._0982.commerce.integration.order.testsupport;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.domain.grouppurchase.GroupPurchase;

import java.util.UUID;

/**
 * 트랜잭션 로직을 담당하는 서비스
 *
 * @Transactional이 적용되어 별도 트랜잭션으로 실행됩니다.
 */
@Service
public class DecreaseQuantityTxService {

    /**
     * 항상 OptimisticLockingFailureException을 던지는 트랜잭션 메서드
     *
     * 테스트 목적: 4번째 시도만 성공하도록 설정
     */
    @Transactional
    public void decreaseQuantityTx(int currentAttempt, UUID groupPurchaseId) {
        if (currentAttempt < 4) {
            System.out.println("에러 발생 (시도 #" + currentAttempt + ")");
            throw new ObjectOptimisticLockingFailureException(GroupPurchase.class, groupPurchaseId);
        } else {
            System.out.println("성공 (시도 #" + currentAttempt + ")");
        }
    }
}
