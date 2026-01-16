package store._0982.ai.application;

import store._0982.ai.feign.commerce.dto.CartInfo;
import store._0982.ai.feign.commerce.dto.OrderInfo;

import java.util.List;
import java.util.UUID;

public interface CommerceQueryPort{
    public List<OrderInfo> getOrders(UUID memberId);
    public List<CartInfo> getCarts(UUID memberId);
}
