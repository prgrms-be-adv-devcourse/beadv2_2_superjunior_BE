package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.order.dto.*;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.application.sellerbalance.SellerBalanceService;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;

import java.util.List;
import java.util.UUID;

import static store._0982.commerce.domain.order.OrderCancellationPolicy.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final OrderRepository orderRepository;

    private final GroupPurchaseService groupPurchaseService;
    private final SellerBalanceService sellerBalanceService;

    private final ApplicationEventPublisher eventPublisher;


    /**
     * 주문 생성
     *
     * @param memberId 고객
     * @param command  주문 command
     * @return OrderRegisterInfo
     */
    public OrderRegisterInfo createOrder(UUID memberId, OrderRegisterCommand command) {
        return orderCommandService.createOrder(memberId, command);
    }

    /**
     * 장바구니 주문 생성
     *
     * @param memberId 고객
     * @param command  주문 command
     * @return OrderRegisterInfo List
     */
    public List<OrderRegisterInfo> createOrderCart(UUID memberId, OrderCartRegisterCommand command) {
        return orderCommandService.createOrderCart(memberId, command);
    }

    /**
     * 주문 상세 조회
     *
     * @param requesterID 요청자
     * @param orderId     주문 id
     * @return OrderDetailInfo
     */
    public OrderDetailInfo getOrderById(UUID requesterID, UUID orderId) {
        return orderQueryService.getOrderById(requesterID, orderId);
    }

    /**
     * 판매자 주문 목록 조회
     *
     * @param sellerId 판매자 id
     * @param pageable pageable
     * @return OrderInfo
     */
    public PageResponse<OrderInfo> getOrdersBySeller(UUID sellerId, Pageable pageable) {
        return orderQueryService.getOrdersBySeller(sellerId, pageable);
    }

    /**
     * 구매자 주문 목록 조회
     *
     * @param memberId 구매자 id
     * @param pageable pageable
     * @return OrderInfo
     */
    public PageResponse<OrderInfo> getOrdersByConsumer(UUID memberId, Pageable pageable) {
        return orderQueryService.getOrdersByConsumer(memberId, pageable);
    }

    @Transactional
    public void cancelOrder(OrderCancelCommand command) {
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        GroupPurchase groupPurchase = groupPurchaseService
                .findByGroupPurchase(order.getGroupPurchaseId());

        if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED) {
            processCancellationBeforeSuccess(order, groupPurchase, command.reason());
            return;
        }

        if (groupPurchase.isInReversedPeriod()) {
            processCancellationWithin48Hours(order, groupPurchase.getSellerId(), command.reason());
            return;
        }

        if (groupPurchase.isInReturnedPeriod()) {
            processReturnAfter48Hours(order, groupPurchase.getSellerId(), command.reason());
            return;
        }
        throw new CustomException(CustomErrorCode.ORDER_CANCELLATION_NOT_ALLOWED);
    }

    private void processCancellationBeforeSuccess(Order order, GroupPurchase groupPurchase, String reason) {
        groupPurchaseService.cancelOrder(groupPurchase.getGroupPurchaseId(), order.getQuantity());

        order.requestCancel();

        RefundAmount refundAmount = calculate(order, CancellationType.BEFORE_GROUP_PURCHASE_SUCCESS);
        publishCancellationEvent(order, reason, refundAmount.refundAmount());
    }

    private void processCancellationWithin48Hours(Order order, UUID sellerId, String reason) {
        order.requestReversed();

        RefundAmount refundAmount = calculate(order, CancellationType.WITHIN_48_HOURS);

        // 판매자에게 수수료 지급
        if (refundAmount.cancellationFee() > 0) {
            sellerBalanceService.addFee(sellerId, refundAmount.cancellationFee());
        }

        publishCancellationEvent(order, reason, refundAmount.refundAmount());
    }

    private void processReturnAfter48Hours(Order order, UUID sellerId, String reason) {
        order.requestReturned();

        RefundAmount refundAmount = calculate(order, CancellationType.AFTER_48_HOURS);

        // 판매자에게 수수료 지급
        if (refundAmount.cancellationFee() > 0) {
            sellerBalanceService.addFee(sellerId, refundAmount.cancellationFee());
        }

        publishCancellationEvent(order, reason, refundAmount.refundAmount());
    }

    private void publishCancellationEvent(Order order, String reason, Long refundAmount) {
        eventPublisher.publishEvent(
                new OrderCancelProcessedEvent(order, reason, refundAmount)
        );
    }

    public List<Order> getAllOrderByMemberId(UUID memberId) {
        return orderQueryService.getAllOrderByMemberId(memberId);
    }
}
