package store._0982.commerce.integration.order.testsupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.common.domain.order.*;

import java.util.UUID;

@Service
public class RetryCancelService {

    @Autowired
    private RetryQuantityService retryQuantityService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CanceledOrderRepository canceledOrderRepository;

    @Transactional
    public void cancelLike(UUID orderId, UUID memberId) {
        Order order = orderRepository.findById(orderId).orElseThrow();

        retryQuantityService.decreaseQuantity(order.getGroupPurchaseId(), order.getQuantity());

        CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                order.getOrderId(),
                memberId,
                order.getSellerId(),
                order.getPaidPrice(),
                0L,
                0L,
                order.getPaidPrice(),
                "test-policy",
                "{}",
                CancelStatus.REQUESTED,
                CancelReason.CHANGE_OF_MIND,
                "fail-fast-snapshot",
                UUID.randomUUID().toString(),
                PaymentMethod.PG
        );

        canceledOrderRepository.save(canceledOrder);
    }
}
