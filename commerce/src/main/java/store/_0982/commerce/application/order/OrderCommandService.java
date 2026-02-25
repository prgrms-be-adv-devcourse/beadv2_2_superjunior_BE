package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.cart.CartService;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService.GroupPurchaseWithProduct;
import store._0982.commerce.application.grouppurchase.ParticipateService;
import store._0982.commerce.application.order.dto.OrderCartRegisterCommand;
import store._0982.commerce.application.order.dto.OrderRegisterCommand;
import store._0982.commerce.application.order.dto.OrderRegisterInfo;
import store._0982.commerce.application.order.event.OrderCartCompletedEvent;
import store._0982.commerce.application.settlement.OrderSettlementService;
import store._0982.commerce.domain.cart.Cart;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.Order;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.log.ServiceLog;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCommandService {

    private final OrderRepository orderRepository;

    private final CartService cartService;
    private final GroupPurchaseService groupPurchaseService;
    private final ParticipateService participateService;

    private final MemberClient memberClient;

    private final ApplicationEventPublisher eventPublisher;
    private final OrderSettlementService orderSettlementService;
    private final TxCreateOrderService txCreateOrderService;

    @ServiceLog
    public OrderRegisterInfo createOrder(UUID memberId, OrderRegisterCommand command) {

        // GroupPurchase, Product 조회
        GroupPurchaseWithProduct validated = groupPurchaseService.getAvailableForOrderWithProduct(command.groupPurchaseId());
        GroupPurchase groupPurchase = validated.groupPurchase();

        // 참여
        participateService.participate(groupPurchase, validated.product(), command.quantity());

        try{
            return txCreateOrderService.create(memberId, command, groupPurchase);
        } catch (DataIntegrityViolationException e) {
            Optional<Order> existingOrder = orderRepository.findByIdempotenceKey(command.requestId());
            if (existingOrder.isPresent()) {
                participateService.rollback(groupPurchase.getGroupPurchaseId(), command.quantity());
                return OrderRegisterInfo.from(existingOrder.get());
            }
            throw e;
        } catch (RuntimeException e) {
            participateService.rollback(groupPurchase.getGroupPurchaseId(), command.quantity());
            throw e;
        }
    }

    @Deprecated
    @Transactional
    @ServiceLog
    public List<OrderRegisterInfo> createOrderCart(UUID memberId, OrderCartRegisterCommand command) {

        // cartId 리스트로 장바구니 아이템들 조회
        List<Cart> carts = cartService.validateAndGetCartForOrder(memberId,command.cartIds());

        // 공동 구매 유효한지 확인
        Set<UUID> groupPurchaseIds = carts.stream()
                .map(Cart::getGroupPurchaseId)
                .collect(Collectors.toSet());

        // 공동 구매 리스트 조회
        Map<UUID, GroupPurchase> purchasesMap = groupPurchaseService.getAvailableGroupPurchasesOrder(groupPurchaseIds);

        // 주문 생성
        List<OrderRegisterInfo> orders = createOrderFromCart(memberId, carts, purchasesMap, command);

        // 장바구니 비우기
        eventPublisher.publishEvent(new OrderCartCompletedEvent(carts));

        return orders;
    }


    @Deprecated
    private List<OrderRegisterInfo> createOrderFromCart(UUID memberId, List<Cart> carts, Map<UUID, GroupPurchase> purchaseMap, OrderCartRegisterCommand command){
        List<Order> orderToSave = new ArrayList<>();

        for(Cart cart: carts){
            GroupPurchase groupPurchase = purchaseMap.get(cart.getGroupPurchaseId());

            if(groupPurchase == null){
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_NOT_FOUND);
            }

            String orderRequestId = command.requestId() + "-" + cart.getCartId();


            //participateService.participate(cart.getGroupPurchaseId(), cart.getQuantity());

            Order order = Order.create(
                    cart.getQuantity(),
                    groupPurchase.getDiscountedPrice(),
                    ((long) cart.getQuantity() * groupPurchase.getDiscountedPrice()),
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

    @Transactional
    public void confirmPurchase(UUID memberId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));

        if (!order.getMemberId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.ORDER_ACCESS_DENIED);
        }

        order.confirmed();
        orderSettlementService.saveConfirmedOrderSettlement(order);
    }
}
