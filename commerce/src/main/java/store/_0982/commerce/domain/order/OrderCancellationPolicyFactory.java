package store._0982.commerce.domain.order;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 취소 정책 Factory
 */
@Slf4j
@Component
public class OrderCancellationPolicyFactory {

    private final Map<String, OrderCancellationPolicy> policyMap;

    public OrderCancellationPolicyFactory(List<OrderCancellationPolicy> policies) {
        this.policyMap = new HashMap<>();

        for (OrderCancellationPolicy policy : policies) {
            String policyType = policy.getPolicyType().name();
            policyMap.put(policyType, policy);
        }
    }

    public OrderCancellationPolicy getPolicy(String policyType) {
        OrderCancellationPolicy policy = policyMap.get(policyType);

        if (policy == null) {
            throw new IllegalArgumentException(
                    "취소 정책을 찾을 수 없습니다: " + policyType +
                    " (사용 가능한 정책: " + policyMap.keySet() + ")"
            );
        }
        return policy;
    }
}
