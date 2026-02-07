package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.GroupPurchaseQuantityService;
import store._0982.commerce.application.order.event.OrderCreateProcessedEvent;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.application.settlement.OrderSettlementService;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.order.*;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.order.PaymentMethod;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.PaymentChangedEvent;
import store._0982.common.kafka.dto.PointChangedEvent;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderPaymentProcessorService {

    private final OrderRepository orderRepository;
    private final CanceledOrderRepository canceledOrderRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;

    private final OrderSettlementService orderSettlementService;
    private final ProductService productService;
    private final GroupPurchaseQuantityService groupPurchaseQuantityService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void processPaymentStatusUpdate(PaymentChangedEvent event){
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        GroupPurchase groupPurchase = groupPurchaseRepository.findById(order.getGroupPurchaseId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        switch(event.getStatus()){
            case PAYMENT_COMPLETED -> {
                if(order.getStatus() != OrderStatus.PENDING) return;
                order.completePayment(PaymentMethod.PG);

                String productName = productService.findByProductName(groupPurchase.getProductId());
                eventPublisher.publishEvent(new OrderCreateProcessedEvent(
                        order,
                        productName
                ));
            }
            case PAYMENT_FAILED -> {
                if(order.getStatus() != OrderStatus.PENDING) return;
                order.markFailed();
                groupPurchaseQuantityService.decreaseQuantity(
                        groupPurchase.getGroupPurchaseId(),
                        order.getQuantity()
                );
            }
            case REFUNDED -> {
                if (order.getStatus() == OrderStatus.CANCELLED) {
                    CanceledOrder canceledOrder = canceledOrderRepository.findByOrderId(order.getOrderId())
                                    .orElseThrow(() -> new CustomException(CustomErrorCode.CANCELED_ORDER_NOT_FOUND));
                    orderSettlementService.saveCanceledOrderSettlement(order, canceledOrder);
                    canceledOrder.markCompleted();
                }
            }
        }
    }

    @Transactional
    public void processPointStatusUpdate(PointChangedEvent event){
        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        switch(event.getStatus()){
            case USED -> {
                order.completePayment(PaymentMethod.POINT);
                GroupPurchase groupPurchase = groupPurchaseRepository.findById(order.getGroupPurchaseId())
                        .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));
                String productName = productService.findByProductName(groupPurchase.getProductId());
                eventPublisher.publishEvent(new OrderCreateProcessedEvent(
                        order,
                        productName
                ));
            }
            case REFUNDED -> {
                if (order.getStatus() == OrderStatus.CANCELLED) {
                    CanceledOrder canceledOrder = canceledOrderRepository.findByOrderId(order.getOrderId())
                            .orElseThrow(() -> new CustomException(CustomErrorCode.CANCELED_ORDER_NOT_FOUND));
                    orderSettlementService.saveCanceledOrderSettlement(order, canceledOrder);
                    canceledOrder.markCompleted();
                }
            }
        }
    }
}
