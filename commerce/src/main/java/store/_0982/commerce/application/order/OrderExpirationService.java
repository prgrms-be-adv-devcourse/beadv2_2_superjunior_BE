package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.GroupPurchaseQuantityService;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.common.domain.order.Order;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationService {

    private final OrderRepository orderRepository;
    private final GroupPurchaseQuantityService groupPurchaseQuantityService;

    @Transactional
    public void expireSingleOrders(Order order){
        order.markExpired();
        orderRepository.save(order);

        groupPurchaseQuantityService.decreaseQuantity(
                order.getGroupPurchaseId(),
                order.getQuantity()
        );

        log.debug("주문 만료 처리 성공: orderId={}", order.getOrderId());
    }
}
