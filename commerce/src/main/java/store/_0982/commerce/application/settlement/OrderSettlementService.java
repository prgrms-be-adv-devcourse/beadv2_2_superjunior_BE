package store._0982.commerce.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderCancellationPolicy;
import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.domain.settlement.OrderSettlement;
import store._0982.commerce.domain.settlement.OrderSettlementRepository;

import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class OrderSettlementService {

    private final OrderSettlementRepository orderSettlementRepository;

    @Transactional
    public void createOrderSettlement(UUID orderId, UUID sellerId, UUID groupPurchaseId, long totalAmount, OrderStatus status) {
        OrderSettlement orderSettlement = OrderSettlement.createOrderSettlement(orderId, sellerId, groupPurchaseId, totalAmount, status);
        orderSettlementRepository.save(orderSettlement);
    }

    @Transactional
    public void saveCanceledOrderSettlement(Order order) {
        OrderCancellationPolicy.CancellationType type = mapCancellationType(order.getStatus());
        if (type == null) {
            return;
        }

        OrderCancellationPolicy.RefundAmount result = OrderCancellationPolicy.calculate(order, type);
        long cancellationFee = result.cancellationFee();

        OrderSettlement orderSettlement = OrderSettlement.createOrderSettlement(
                order.getOrderId(),
                order.getSellerId(),
                order.getGroupPurchaseId(),
                cancellationFee,
                order.getStatus()
        );
        orderSettlementRepository.save(orderSettlement);
    }

    private OrderCancellationPolicy.CancellationType mapCancellationType(OrderStatus status) {
        return switch (status) {
            case CANCELLED -> OrderCancellationPolicy.CancellationType.BEFORE_GROUP_PURCHASE_SUCCESS;
            case REVERSED -> OrderCancellationPolicy.CancellationType.WITHIN_48_HOURS;
            case REFUNDED -> OrderCancellationPolicy.CancellationType.AFTER_48_HOURS;
            default -> null;
        };
    }
}
