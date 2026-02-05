package store._0982.commerce.domain.order.policy;

import org.springframework.stereotype.Component;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderCancellationPolicy;

/**
 * 공동구매 성공 후 48시간 ~ 2주 이내 반품 정책
 * - 수수료: 20%
 * - 택배비: 6,000원
 * - 환불: 결제 금액의 80% - 택배비
 * - 적용 시점: 공동구매 성공 후 48시간 ~ 2주 이내
 */
@Component
public class RefundOrderCancellationPolicy implements OrderCancellationPolicy {

    private static final double CANCELLATION_FEE_RATE = 0.20;  // 20%
    private static final long SHIPPING_FEE = 6000L;            // 택배비
    private static final String POLICY_ID = "CANCEL_POLICY_REFUND_V1";

    @Override
    public RefundAmount calculate(Order order) {
        long paidAmount = order.getPaidPrice();
        long fee = (long) (paidAmount * CANCELLATION_FEE_RATE);
        long refund = paidAmount - fee - SHIPPING_FEE;

        return new RefundAmount(refund, fee, SHIPPING_FEE);
    }

    @Override
    public String getPolicyId() {
        return POLICY_ID;
    }

    @Override
    public PolicyType getPolicyType() {
        return PolicyType.REFUND;
    }
}
