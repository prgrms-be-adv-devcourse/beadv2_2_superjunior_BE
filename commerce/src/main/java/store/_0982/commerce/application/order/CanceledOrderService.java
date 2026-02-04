package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.application.settlement.OrderSettlementService;
import store._0982.commerce.domain.order.*;
import store._0982.commerce.domain.order.OrderCancellationPolicy.RefundAmount;
import store._0982.commerce.domain.order.policy.RefundOrderCancellationPolicy;
import store._0982.commerce.domain.order.policy.ReversalOrderCancellationPolicy;
import store._0982.commerce.domain.order.policy.VoidOrderCancellationPolicy;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CanceledOrderService {

    private final ProductService productService;
    private final GroupPurchaseService groupPurchaseService;
    private final OrderSettlementService orderSettlementService;

    private final OrderRepository orderRepository;
    private final CanceledOrderRepository canceledOrderRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final VoidOrderCancellationPolicy voidOrderCancellationPolicy;
    private final ReversalOrderCancellationPolicy reversalOrderCancellationPolicy;
    private final RefundOrderCancellationPolicy refundOrderCancellationPolicy;

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 10,
            backoff = @Backoff(
                    delay = 50,
                    maxDelay = 500,
                    random = true
            )
    )
    @ServiceLog
    @Transactional
    public void cancelOrder(OrderCancelCommand command) {
        if (canceledOrderRepository.existsByIdempotencyKey(command.idempotencyKey())
                || canceledOrderRepository.existsByOrderId(command.orderId())) {
            return;
        }

        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() != OrderStatus.PAYMENT_COMPLETED) {
            throw new CustomException(CustomErrorCode.CANNOT_CANCEL_ORDER_INVALID_STATUS);
        }

        if (!command.memberId().equals(order.getMemberId())) {
            throw new CustomException(CustomErrorCode.ORDER_ACCESS_DENIED);
        }

        GroupPurchase groupPurchase = groupPurchaseService
                .findByGroupPurchase(order.getGroupPurchaseId());

        String productName = productService.findByProductName(groupPurchase.getProductId());
        order.requestCanceledAt();

        if (command.reason().isBuyerFault()) {
            groupPurchaseService.decreaseQuantity(groupPurchase.getGroupPurchaseId(), order.getQuantity());

            if (groupPurchase.isInVoidPeriod()) {
                RefundAmount refundAmount = voidOrderCancellationPolicy.calculate(order);
                CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                        order.getOrderId(),
                        command.memberId(),
                        order.getPaidPrice(),
                        refundAmount.cancellationFee(),
                        refundAmount.shippingFee(),
                        refundAmount.refundAmount(),
                        voidOrderCancellationPolicy.getPolicyId(),
                        voidOrderCancellationPolicy.buildSnapshot(refundAmount),
                        command.reason(),
                        command.detailReason(),
                        command.idempotencyKey(),
                        order.getPaymentMethod()
                );
                canceledOrderRepository.save(canceledOrder);
                publishCancellationEvent(order, command.detailReason(), refundAmount.refundAmount(), productName);
                return;
            }

            if (groupPurchase.isInReversedPeriod(order.getCanceledAt())) {
                RefundAmount refundAmount = reversalOrderCancellationPolicy.calculate(order);
                CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                        order.getOrderId(),
                        command.memberId(),
                        order.getPaidPrice(),
                        refundAmount.cancellationFee(),
                        refundAmount.shippingFee(),
                        refundAmount.refundAmount(),
                        reversalOrderCancellationPolicy.getPolicyId(),
                        reversalOrderCancellationPolicy.buildSnapshot(refundAmount),
                        command.reason(),
                        command.detailReason(),
                        command.idempotencyKey(),
                        order.getPaymentMethod()
                );
                canceledOrderRepository.save(canceledOrder);
                publishCancellationEvent(order, command.detailReason(), refundAmount.refundAmount(), productName);
                return;
            }

            if (groupPurchase.isInReturnedPeriod(order.getCanceledAt())) {
                RefundAmount refundAmount = refundOrderCancellationPolicy.calculate(order);
                CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                        order.getOrderId(),
                        command.memberId(),
                        order.getPaidPrice(),
                        refundAmount.cancellationFee(),
                        refundAmount.shippingFee(),
                        refundAmount.refundAmount(),
                        refundOrderCancellationPolicy.getPolicyId(),
                        refundOrderCancellationPolicy.buildSnapshot(refundAmount),
                        command.reason(),
                        command.detailReason(),
                        command.idempotencyKey(),
                        order.getPaymentMethod()
                );
                canceledOrderRepository.save(canceledOrder);
                publishCancellationEvent(order, command.detailReason(), refundAmount.refundAmount(), productName);
                return;
            }
        } else if (command.reason().isSellerFault()) {
            RefundAmount refundAmount = voidOrderCancellationPolicy.calculate(order);
            CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                    order.getOrderId(),
                    command.memberId(),
                    order.getPaidPrice(),
                    refundAmount.cancellationFee(),
                    refundAmount.shippingFee(),
                    refundAmount.refundAmount(),
                    voidOrderCancellationPolicy.getPolicyId(),
                    voidOrderCancellationPolicy.buildSnapshot(refundAmount),
                    command.reason(),
                    command.detailReason(),
                    command.idempotencyKey(),
                    order.getPaymentMethod()
            );
            canceledOrderRepository.save(canceledOrder);
            publishCancellationEvent(order, command.detailReason(), refundAmount.refundAmount(), productName);
            return;
        }

        throw new CustomException(CustomErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
    }

    private void publishCancellationEvent(Order order, String reason, Long refundAmount, String productName) {
        eventPublisher.publishEvent(
                new OrderCancelProcessedEvent(order, reason, refundAmount, productName)
        );
    }

    @ServiceLog
    @Transactional
    public void retryCancelOrder() {
        List<OrderStatus> pendingStatuses = List.of(
                OrderStatus.CANCEL_REQUESTED,
                OrderStatus.REVERSE_REQUESTED,
                OrderStatus.REFUND_REQUESTED
        );

        OffsetDateTime minutesAgo = OffsetDateTime.now().minusMinutes(15);
        List<Order> pendingOrders = orderRepository.findAllByStatusInAndCancelRequestAtBefore(pendingStatuses, minutesAgo);
        if (pendingOrders.isEmpty()) {
            return;
        }

        for (Order order : pendingOrders) {
            OrderCancellationPolicy.CancellationType cancellationType = mapCancellationType(order.getStatus());
            if (cancellationType == null) {
                continue;
            }

            GroupPurchase groupPurchase = groupPurchaseService
                    .findByGroupPurchase(order.getGroupPurchaseId());
            String productName = productService.findByProductName(groupPurchase.getProductId());

            OrderCancellationPolicy.RefundAmount calculated = calculate(order, cancellationType);
            publishCancellationEvent(order, "retry-cancel", calculated.refundAmount(), productName) ;
        }
    }
}
