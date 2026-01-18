package store._0982.batch.infrastructure.client.commerce;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.batch.application.commerce.CommerceQueryPort;
import store._0982.batch.domain.ai.CartVector;
import store._0982.batch.domain.ai.OrderVector;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CommerceQueryFeignAdapter implements CommerceQueryPort {

    private final CommerceFeignClient commerceFeignClient;

    @Override
    public List<OrderVector> getOrders(UUID memberId) {
        return commerceFeignClient.getOrdersConsumer(memberId).data();
    }

    @Override
    public List<CartVector> getCarts(UUID memberId) {
        return commerceFeignClient.getCarts(memberId).data();
    }
}
