package store._0982.commerce.domain.order.policy;

import org.springframework.stereotype.Component;
import store._0982.commerce.domain.order.OrderCancellationPolicy;
import store._0982.common.domain.order.Order;

/**
 * 공동구매 성공 후 48시간 이내 취소 정책 (철회)
 * - 수수료: 20%
 * - 환불: 결제 금액의 80%
 * - 적용 시점: 공동구매 성공 후 48시간 이내
 */
@Component
public class ReversalOrderCancellationPolicy implements OrderCancellationPolicy {

    private static final double CANCELLATION_FEE_RATE = 0.20;  // 20%
    private static final String POLICY_ID = "CANCEL_POLICY_REVERSAL_V1";

    @Override
    public RefundAmount calculate(Order order) {
        long paidAmount = order.getPaidPrice();
        long fee = (long) (paidAmount * CANCELLATION_FEE_RATE);
        long refund = paidAmount - fee;

        return new RefundAmount(refund, fee, 0L);
    }

    @Override
    public String getPolicyId() {
        return POLICY_ID;
    }

    @Override
    public PolicyType getPolicyType() {
        return PolicyType.REVERSAL;
    }
}
