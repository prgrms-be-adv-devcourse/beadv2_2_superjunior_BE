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
import store._0982.commerce.domain.order.CancelReason;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
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

        if (command.reason() == CancelReason.CHANGE_OF_MIND) {
            groupPurchaseService.decreaseQuantity(groupPurchase.getGroupPurchaseId(), order.getQuantity());

            if (groupPurchase.isInVoidPeriod()) {
                processCancellationBeforeSuccess(order, command.reason(), productName);
                return;
            }

            if (groupPurchase.isInReversedPeriod(order.getCanceledAt())) {
                processCancellationWithin48Hours(order, command.reason(), productName);
                return;
            }

            if (groupPurchase.isInReturnedPeriod(order.getCanceledAt())) {
                processReturnAfter48Hours(order, command.reason(), productName);
                return;
            }
        } else if (command.reason() == CancelReason.DELIVERY_DELAY ||
                command.reason() == CancelReason.OUT_OF_STOCK ||
                command.reason() == CancelReason.PRODUCT_DEFECT) {

            orderSettlementService.saveCanceledOrderSettlement(order);
        }

        throw new CustomException(CustomErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
    }

    private void processCancellationBeforeSuccess(Order order, String reason, String productName) {

        OrderCancellationPolicy.RefundAmount refundAmount = calculate(order, OrderCancellationPolicy.CancellationType.BEFORE_GROUP_PURCHASE_SUCCESS);
        publishCancellationEvent(order, reason, refundAmount.refundAmount(), productName);
    }

    private void processCancellationWithin48Hours(Order order, String reason, String productName) {
        order.requestReversed();

        OrderCancellationPolicy.RefundAmount refundAmount = calculate(order, OrderCancellationPolicy.CancellationType.WITHIN_48_HOURS);
        publishCancellationEvent(order, reason, refundAmount.refundAmount(), productName);
    }

    private void processReturnAfter48Hours(Order order, String reason, String productName) {
        order.requestReturned();

        OrderCancellationPolicy.RefundAmount refundAmount = calculate(order, OrderCancellationPolicy.CancellationType.AFTER_48_HOURS);
        publishCancellationEvent(order, reason, refundAmount.refundAmount(), productName);
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
