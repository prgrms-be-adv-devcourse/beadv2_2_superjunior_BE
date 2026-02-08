package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.GroupPurchaseQuantityService;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.application.order.dto.OrderCancelInfo;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.domain.order.*;
import store._0982.commerce.domain.order.OrderCancellationPolicy.RefundAmount;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.CanceledOrder;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CanceledOrderService {

    private final ProductService productService;
    private final GroupPurchaseService groupPurchaseService;
    private final GroupPurchaseQuantityService groupPurchaseQuantityService;

    private final OrderRepository orderRepository;
    private final CanceledOrderRepository canceledOrderRepository;

    private final ApplicationEventPublisher eventPublisher;

    private final OrderCancellationPolicyResolver orderCancellationPolicyResolver;

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

        groupPurchaseService.decreaseQuantity(groupPurchase.getGroupPurchaseId(), order.getQuantity());

        order.requestCanceledAt();
        OrderCancellationPolicy policy = orderCancellationPolicyResolver.resolve(groupPurchase, order, command.reason());
        RefundAmount refundAmount = policy.calculate(order);

        CancelStatus status = command.reason().isBuyerFault()
                ? CancelStatus.REQUESTED
                : CancelStatus.PENDING;

        CanceledOrder canceledOrder = CanceledOrder.createCanceledOrder(
                order.getOrderId(),
                command.memberId(),
                order.getSellerId(),
                order.getPaidPrice(),
                refundAmount.cancellationFee(),
                refundAmount.shippingFee(),
                refundAmount.refundAmount(),
                policy.getPolicyId(),
                policy.buildSnapshot(refundAmount),
                status,
                command.reason(),
                command.detailReason(),
                command.idempotencyKey(),
                order.getPaymentMethod()
        );
        canceledOrderRepository.save(canceledOrder);

        if (command.reason().isBuyerFault()) {
            publishCancellationEvent(canceledOrder, productName);
        }
    }

    @Transactional
    public OrderCancelInfo approvePendingOrder(UUID memberId, UUID orderId) {
        CanceledOrder findCanceledOrder = canceledOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CANCELED_ORDER_NOT_FOUND));

        Order findOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        if (!findOrder.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.NON_SELLER_ACCESS_DENIED);
        }

        GroupPurchase groupPurchase = groupPurchaseService
                .findByGroupPurchase(findOrder.getGroupPurchaseId());
        String productName = productService.findByProductName(groupPurchase.getProductId());

        findCanceledOrder.markApproved();
        publishCancellationEvent(findCanceledOrder, productName);

        return OrderCancelInfo.toOrderCancelInfo(findCanceledOrder);
    }

    @Transactional
    public OrderCancelInfo rejectPendingOrder(UUID memberId, UUID orderId) {
        CanceledOrder findCanceledOrder = canceledOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.CANCELED_ORDER_NOT_FOUND));

        if (!findCanceledOrder.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.NON_SELLER_ACCESS_DENIED);
        }

        findCanceledOrder.markRejected();
        return OrderCancelInfo.toOrderCancelInfo(findCanceledOrder);
    }

    @ServiceLog
    @Transactional
    public void retryCancelOrder() {
        List<CancelStatus> pendingStatuses = List.of(
                CancelStatus.REQUESTED,
                CancelStatus.APPROVED
        );

        OffsetDateTime minutesAgo = OffsetDateTime.now().minusMinutes(15);
        List<CanceledOrder> pendingOrders =
                canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(pendingStatuses, minutesAgo);

        if (pendingOrders.isEmpty()) {
            return;
        }

        for (CanceledOrder canceledOrder : pendingOrders) {
            Order order = orderRepository.findById(canceledOrder.getOrderId())
                    .orElse(null);
            if (order == null) {
                continue;
            }

            OrderCancellationPolicy policy = orderCancellationPolicyResolver.resolveByPolicyId(canceledOrder.getPolicyId());
            if (policy == null) {
                continue;
            }

            GroupPurchase groupPurchase = groupPurchaseService
                    .findByGroupPurchase(order.getGroupPurchaseId());
            String productName = productService.findByProductName(groupPurchase.getProductId());

            publishCancellationEvent(canceledOrder, productName);
        }
    }

    @ServiceLog
    @Transactional
    public void autoCancelOrder() {
        List<CancelStatus> pendingStatuses = List.of(
                CancelStatus.PENDING
        );

        OffsetDateTime twoDaysAgo = OffsetDateTime.now().minusDays(2);
        List<CanceledOrder> pendingOrders =
                canceledOrderRepository.findAllByStatusInAndCanceledAtBefore(pendingStatuses, twoDaysAgo);

        if (pendingOrders.isEmpty()) {
            return;
        }

        for (CanceledOrder canceledOrder : pendingOrders) {
            Order order = orderRepository.findById(canceledOrder.getOrderId())
                    .orElse(null);
            if (order == null) {
                continue;
            }

            OrderCancellationPolicy policy = orderCancellationPolicyResolver.resolveByPolicyId(canceledOrder.getPolicyId());
            if (policy == null) {
                continue;
            }

            GroupPurchase groupPurchase = groupPurchaseService
                    .findByGroupPurchase(order.getGroupPurchaseId());
            String productName = productService.findByProductName(groupPurchase.getProductId());

            canceledOrder.markApproved();
            publishCancellationEvent(canceledOrder, productName);
        }
    }

    private void publishCancellationEvent(CanceledOrder canceledOrder, String productName) {
        eventPublisher.publishEvent(
                new OrderCancelProcessedEvent(canceledOrder, productName)
        );
    }
}
