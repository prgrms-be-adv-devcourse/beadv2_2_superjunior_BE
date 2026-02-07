package store._0982.commerce.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.domain.order.CanceledOrder;
import store._0982.commerce.domain.settlement.OrderSettlementRepository;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.settlement.OrderSettlement;
import store._0982.common.domain.settlement.OrderSettlementStatus;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class OrderSettlementService {

    private final OrderSettlementRepository orderSettlementRepository;

    @Transactional
    public void saveConfirmedOrderSettlement(Order order) {
        OrderSettlement orderSettlement = OrderSettlement.createOrderSettlement(
                order.getOrderId(),
                order.getSellerId(),
                order.getGroupPurchaseId(),
                order.getPrice() * order.getQuantity(),
                OrderSettlementStatus.COMPLETED);

        orderSettlementRepository.save(orderSettlement);
    }

    @Transactional
    public void saveCanceledOrderSettlement(Order order, CanceledOrder canceledOrder) {
        if (canceledOrder.getReason().isSellerFault()) {
            saveCanceledOrderSettlementBySeller(order, canceledOrder);
            return;
        }
        if (canceledOrder.getReason().isBuyerFault()) {
            saveCanceledOrderSettlementByBuyer(order, canceledOrder);
        }
    }

    private void saveCanceledOrderSettlementByBuyer(Order order, CanceledOrder canceledOrder) {
        OrderSettlement orderSettlement = OrderSettlement.createOrderSettlement(
                canceledOrder.getOrderId(),
                order.getSellerId(),
                order.getGroupPurchaseId(),
                canceledOrder.getCancelFeeAmount(),
                OrderSettlementStatus.BUYER_CANCEL
        );
        orderSettlementRepository.save(orderSettlement);
    }

    private void saveCanceledOrderSettlementBySeller(Order order, CanceledOrder canceledOrder) {
        OrderSettlement orderSettlement = OrderSettlement.createOrderSettlement(
                canceledOrder.getOrderId(),
                order.getSellerId(),
                order.getGroupPurchaseId(),
                canceledOrder.getShippingFeeAmount() * (-1),
                OrderSettlementStatus.SELLER_CANCEL
        );
        orderSettlementRepository.save(orderSettlement);
    }
}
