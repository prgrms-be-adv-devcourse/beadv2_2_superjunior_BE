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

    public void validateOrderPayable(UUID memberId, UUID orderId, long amount) {
        OrderInfo orderInfo = commerceServiceClient.getOrder(orderId, memberId);
        orderInfo.validatePayable(memberId, orderId, amount);
    }
}
