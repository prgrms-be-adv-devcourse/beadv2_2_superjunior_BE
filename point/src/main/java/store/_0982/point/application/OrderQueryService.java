package store._0982.point.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.point.client.CommerceServiceClient;
import store._0982.point.client.dto.OrderInfo;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderQueryService {

    private final CommerceServiceClient commerceServiceClient;

    public OrderInfo getOrderDetails(UUID memberId, UUID orderId) {
        return commerceServiceClient.getOrder(orderId, memberId);
    }

    public void validateOrderPayable(UUID memberId, UUID orderId, long amount) {
        OrderInfo orderInfo = getOrderDetails(memberId, orderId);
        orderInfo.validatePayable(memberId, orderId, amount);
    }
}
