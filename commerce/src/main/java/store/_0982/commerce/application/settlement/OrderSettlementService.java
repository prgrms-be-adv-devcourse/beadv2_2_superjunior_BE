package store._0982.commerce.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
}
