package store._0982.commerce.application.order;

import org.springframework.stereotype.Component;
import store._0982.commerce.domain.order.CancelReason;
import store._0982.commerce.domain.order.OrderCancellationPolicy;
import store._0982.commerce.domain.order.policy.RefundOrderCancellationPolicy;
import store._0982.commerce.domain.order.policy.ReversalOrderCancellationPolicy;
import store._0982.commerce.domain.order.policy.VoidOrderCancellationPolicy;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.Order;
import store._0982.common.exception.CustomException;

import java.util.Map;

@Component
public class OrderCancellationPolicyResolver {

    private final VoidOrderCancellationPolicy voidPolicy;
    private final ReversalOrderCancellationPolicy reversalPolicy;
    private final RefundOrderCancellationPolicy refundPolicy;

    private final Map<String, OrderCancellationPolicy> policyMap;

    public OrderCancellationPolicyResolver(
            VoidOrderCancellationPolicy voidPolicy,
            ReversalOrderCancellationPolicy reversalPolicy,
            RefundOrderCancellationPolicy refundPolicy) {

        this.voidPolicy = voidPolicy;
        this.reversalPolicy = reversalPolicy;
        this.refundPolicy = refundPolicy;

        this.policyMap = Map.of(
                voidPolicy.getPolicyId(), voidPolicy,
                reversalPolicy.getPolicyId(), reversalPolicy,
                refundPolicy.getPolicyId(), refundPolicy
        );
    }

    public OrderCancellationPolicy resolve(
            GroupPurchase groupPurchase,
            Order order,
            CancelReason reason) {

        // Buyer 귀책
        if (reason.isBuyerFault()) {
            if (groupPurchase.isInVoidPeriod()) {
                return voidPolicy;
            }
            if (groupPurchase.isInReversedPeriod(order.getCanceledAt())) {
                return reversalPolicy;
            }
            if (groupPurchase.isInReturnedPeriod(order.getCanceledAt())) {
                return refundPolicy;
            }
        }

        // Seller 귀책
        if (reason.isSellerFault()) {
            return voidPolicy;
        }

        throw new CustomException(CustomErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
    }

    public OrderCancellationPolicy resolveByPolicyId(String policyId) {
        return policyMap.get(policyId);
    }
}
