package store._0982.commerce.domain.order.policy;

import org.springframework.stereotype.Component;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderCancellationPolicy;

/**
 * 공동구매 성공 전 취소 정책 (무효화)
 * - 수수료: 0%
 * - 환불: 전액 환불
 * - 적용 시점: 공동구매 성공 전, 공동구매 실패
 */
@Component
public class VoidOrderCancellationPolicy implements OrderCancellationPolicy {

    @Override
    public RefundAmount calculate(Order order) {
        long paidAmount = order.getPaidPrice();

        return new RefundAmount(
                paidAmount,  // 전액 환불
                0L,          // 수수료 없음
                0L
        );
    }

    @Override
    public PolicyType getPolicyType() {
        return PolicyType.VOID;
    }
}
