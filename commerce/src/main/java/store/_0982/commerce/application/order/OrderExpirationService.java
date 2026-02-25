package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.GroupPurchaseQuantityService;
import store._0982.commerce.application.grouppurchase.ParticipateService;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.order.Order;
import store._0982.common.exception.CustomException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExpirationService {

    private final OrderRepository orderRepository;
    private final GroupPurchaseQuantityService groupPurchaseQuantityService;
    private final ParticipateService participateService;

    @Transactional
    public void expireSingleOrders(Order order){
        expireSingleOrders(order.getOrderId());
    }

    @Transactional
    public void expireSingleOrders(UUID orderId){
        int updated = orderRepository.markOrderExpired(orderId);

        if(updated == 0){
            return;
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        participateService.rollback(
                order.getGroupPurchaseId(),
                order.getQuantity()
        );

        log.debug("주문 만료 처리 성공: orderId={}", order.getOrderId());
    }
}
