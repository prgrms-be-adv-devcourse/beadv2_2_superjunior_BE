package store._0982.commerce.application.order;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.cart.CartService;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.ParticipateService;
import store._0982.commerce.application.order.dto.OrderCancelCommand;
import store._0982.commerce.application.order.dto.OrderCartRegisterCommand;
import store._0982.commerce.application.order.dto.OrderRegisterCommand;
import store._0982.commerce.application.order.dto.OrderRegisterInfo;
import store._0982.commerce.application.order.event.OrderCancelProcessedEvent;
import store._0982.commerce.application.order.event.OrderCartCompletedEvent;
import store._0982.commerce.application.order.event.OrderCreateProcessedEvent;
import store._0982.commerce.application.product.ProductService;
import store._0982.commerce.application.sellerbalance.SellerBalanceService;
import store._0982.commerce.domain.cart.Cart;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderCancellationPolicy;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.infrastructure.client.member.dto.ProfileInfo;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.log.ServiceLog;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static store._0982.commerce.domain.order.OrderCancellationPolicy.calculate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderCommandService {

    private final OrderRepository orderRepository;

    private final CartService cartService;
    private final ProductService productService;
    private final GroupPurchaseService groupPurchaseService;
    private final ParticipateService participateService;
    private final SellerBalanceService sellerBalanceService;

    private final MemberClient memberClient;

    private final ApplicationEventPublisher eventPublisher;

    @ServiceLog
    @Transactional
    public OrderRegisterInfo createOrder(UUID memberId, OrderRegisterCommand command) {

        // 이미 처리 요청된 주문인지 확인
        if(orderRepository.existsByIdempotencyKey(command.requestId())){
            throw new CustomException(CustomErrorCode.DUPLICATE_ORDER);
        }

        // 주문자 존재 여부
        String memberName = validateMember(memberId);

        // 공동 구매 존재 여부
        GroupPurchase groupPurchase = validateGroupPurchase(command.groupPurchaseId());

        // 참여
        participateService.participate(groupPurchase.getGroupPurchaseId(), command.quantity(), memberName, command.requestId());

        // order 생성
        Order order = Order.create(
                command.quantity(),
                groupPurchase.getDiscountedPrice(),
                memberId,
                command.address(),
                command.addressDetail(),
                command.postalCode(),
                command.receiverName(),
                command.sellerId(),
                groupPurchase.getGroupPurchaseId(),
                command.requestId()
        );

        Order savedOrder = orderRepository.save(order);

        String productName = productService.getProductInfo(groupPurchase.getProductId()).name();
        eventPublisher.publishEvent(new OrderCreateProcessedEvent(
                savedOrder,
                productName
        ));

        return OrderRegisterInfo.from(savedOrder);

    }

    @Transactional
    @ServiceLog
    public List<OrderRegisterInfo> createOrderCart(UUID memberId, OrderCartRegisterCommand command) {
        // 주문자 존재 여부
        String memberName = validateMember(memberId);

        // cartId 리스트로 장바구니 아이템들 조회
        List<Cart> carts = cartService.validateAndGetCartForOrder(memberId,command.cartIds());

        // 공동 구매 유효한지 확인
        Set<UUID> groupPurchaseIds = carts.stream()
                .map(Cart::getGroupPurchaseId)
                .collect(Collectors.toSet());

        // 공동 구매 리스트 조회
        Map<UUID, GroupPurchase> purchasesMap = groupPurchaseService.getAvailableGroupPurchasesOrder(groupPurchaseIds);

        // 주문 생성
        List<OrderRegisterInfo> orders = createOrderFromCart(memberId, carts, purchasesMap, command, memberName);

        // 장바구니 비우기
        eventPublisher.publishEvent(new OrderCartCompletedEvent(carts));

        return orders;
    }


    private String validateMember(UUID memberId) {
        try {
            ResponseDto<ProfileInfo> member = memberClient.getMember(memberId);
            return member.data().name();
        } catch (FeignException.NotFound e) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_FOUND);
        } catch (FeignException e) {
            log.error("회원 조회 실패 : memberId={}, status={}", memberId, e.status());
            throw new RuntimeException("회원 정보 조회 오류");
        }
    }

    private GroupPurchase validateGroupPurchase(UUID groupPurchaseId) {
        return groupPurchaseService.getAvailableForOrder(groupPurchaseId);
    }

    private List<OrderRegisterInfo> createOrderFromCart(UUID memberId, List<Cart> carts, Map<UUID, GroupPurchase> purchaseMap, OrderCartRegisterCommand command, String memberName){
        List<Order> orderToSave = new ArrayList<>();

        for(Cart cart: carts){
            GroupPurchase groupPurchase = purchaseMap.get(cart.getGroupPurchaseId());

            if(groupPurchase == null){
                throw new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND);
            }

            String orderRequestId = command.requestId() + "-" + cart.getCartId();


            participateService.participate(cart.getGroupPurchaseId(), cart.getQuantity(), memberName, orderRequestId);

            Order order = Order.create(
                    cart.getQuantity(),
                    groupPurchase.getDiscountedPrice(),
                    memberId,
                    command.address(),
                    command.addressDetail(),
                    command.postalCode(),
                    command.receiverName(),
                    groupPurchase.getSellerId(),
                    groupPurchase.getGroupPurchaseId(),
                    orderRequestId
            );

            orderToSave.add(order);
        }

        List<Order> savedOrders = orderRepository.saveAll(orderToSave);

        return savedOrders.stream()
                .map(OrderRegisterInfo::from)
                .collect(Collectors.toList());
    }

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
        Order order = orderRepository.findById(command.orderId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        if (!command.memberId().equals(order.getMemberId())) {
            throw new CustomException(CustomErrorCode.ORDER_ACCESS_DENIED);
        }

        GroupPurchase groupPurchase = groupPurchaseService
                .findByGroupPurchase(order.getGroupPurchaseId());

        groupPurchaseService.decreaseQuantity(groupPurchase.getGroupPurchaseId(), order.getQuantity());

        if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED) {
            processCancellationBeforeSuccess(order, command.reason());
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

    private void processCancellationBeforeSuccess(Order order, String reason) {
        order.requestCancel();

        OrderCancellationPolicy.RefundAmount refundAmount = calculate(order, OrderCancellationPolicy.CancellationType.BEFORE_GROUP_PURCHASE_SUCCESS);
        publishCancellationEvent(order, reason, refundAmount.refundAmount());
    }

    private void processCancellationWithin48Hours(Order order, UUID sellerId, String reason) {
        order.requestReversed();

        OrderCancellationPolicy.RefundAmount refundAmount = calculate(order, OrderCancellationPolicy.CancellationType.WITHIN_48_HOURS);
        publishCancellationEvent(order, reason, refundAmount.refundAmount());
    }

    private void processReturnAfter48Hours(Order order, UUID sellerId, String reason) {
        order.requestReturned();

        OrderCancellationPolicy.RefundAmount refundAmount = calculate(order, OrderCancellationPolicy.CancellationType.AFTER_48_HOURS);
        publishCancellationEvent(order, reason, refundAmount.refundAmount());
    }

    private void publishCancellationEvent(Order order, String reason, Long refundAmount) {
        eventPublisher.publishEvent(
                new OrderCancelProcessedEvent(order, reason, refundAmount)
        );
    }
  
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

            OrderCancellationPolicy.RefundAmount calculated = calculate(order, cancellationType);
            publishCancellationEvent(order, "retry-cancel", calculated.refundAmount());
        }
    }

    private OrderCancellationPolicy.CancellationType mapCancellationType(OrderStatus status) {
        return switch (status) {
            case CANCEL_REQUESTED -> OrderCancellationPolicy.CancellationType.BEFORE_GROUP_PURCHASE_SUCCESS;
            case REVERSE_REQUESTED -> OrderCancellationPolicy.CancellationType.WITHIN_48_HOURS;
            case REFUND_REQUESTED -> OrderCancellationPolicy.CancellationType.AFTER_48_HOURS;
            default -> null;
        };
    }
  
    @ServiceLog
    @Transactional
    public void processGroupPurchaseFailure(UUID groupPurchaseId){
        orderRepository.bulkMarkGroupPurchaseFail(groupPurchaseId);
    }

    @ServiceLog
    @Transactional
    public void handleUpdatedGroupPurchase(GroupPurchaseEvent event){
        switch(event.getGroupPurchaseStatus()){
            case SUCCESS -> {
                orderRepository.bulkMarkGroupPurchaseSuccess(event.getId());
            }
        }
    }
}
