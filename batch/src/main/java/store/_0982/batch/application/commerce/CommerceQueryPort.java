package store._0982.batch.application.commerce;

import store._0982.batch.domain.ai.CartVector;
import store._0982.batch.domain.ai.OrderVector;

import java.util.List;
import java.util.UUID;

public interface CommerceQueryPort {
    List<OrderVector> getOrders(UUID memberId);
    List<CartVector> getCarts(UUID memberId);
}
