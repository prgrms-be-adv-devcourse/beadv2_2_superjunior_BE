package store._0982.ai.feign.commerce;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.ai.application.CommerceQueryPort;
import store._0982.ai.feign.commerce.dto.CartInfo;
import store._0982.ai.feign.commerce.dto.OrderInfo;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommerceQueryFeignAdapter implements CommerceQueryPort {
    private final CommerceFeignClient commerceFeignClient;

    @Override
    public List<OrderInfo> getOrders(UUID memberId) {
        return commerceFeignClient.getOrdersConsumer(memberId).data();
    }

    @Override
    public List<CartInfo> getCarts(UUID memberId) {
        return commerceFeignClient.getCarts(memberId).data();
    }
}
