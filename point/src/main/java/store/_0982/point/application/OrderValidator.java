package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.point.client.CommerceServiceClient;
import store._0982.point.client.dto.OrderInfo;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderValidator {

    private final CommerceServiceClient commerceServiceClient;

    public void validateOrderDeductible(UUID memberId, UUID orderId, long amount) {
        OrderInfo orderInfo = commerceServiceClient.getOrder(orderId, memberId);
        orderInfo.validateDeductible(memberId, orderId, amount);
    }

    public void validateOrderConfirmable(UUID memberId, UUID orderId, long amount) {
        OrderInfo order = commerceServiceClient.getOrder(orderId, memberId);
        order.validateConfirmable(memberId, orderId, amount);
    }
}
