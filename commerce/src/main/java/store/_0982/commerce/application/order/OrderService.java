package store._0982.commerce.application.order;

import feign.FeignException;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.cart.CartService;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.ParticipateService;
import store._0982.commerce.application.order.dto.*;
import store._0982.commerce.application.order.event.OrderCartCompletedEvent;
import store._0982.commerce.domain.cart.Cart;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.infrastructure.client.member.dto.ProfileInfo;
import store._0982.commerce.infrastructure.client.payment.PaymentClient;
import store._0982.commerce.infrastructure.client.payment.dto.PointReturnRequest;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final GroupPurchaseService groupPurchaseService;
    private final ParticipateService participateService;
    private final MemberClient memberClient;
    private final PaymentClient paymentClient;
    private final OtelTracer.EventPublisher eventPublisher;

    /**
     * 주문 생성
     *
     * @param memberId 고객
     * @param command  주문 command
     * @return OrderRegisterInfo
     */
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

        return OrderRegisterInfo.from(savedOrder);

    }

    /**
     * 장바구니 주문 생성
     *
     * @param memberId 고객
     * @param command  주문 command
     * @return OrderRegisterInfo List
     */
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

    /**
     * 주문 상세 조회
     *
     * @param requesterID 요청자
     * @param orderId     주문 id
     * @return OrderDetailInfo
     */
    public OrderDetailInfo getOrderById(UUID requesterID, UUID orderId) {
        Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));
        log.info("getOrderById - orderId: {}", orderId);
        if (!order.getMemberId().equals(requesterID)
                && !order.getSellerId().equals(requesterID)) {
            throw new CustomException(CustomErrorCode.ORDER_ACCESS_DENIED);
        }

        return OrderDetailInfo.from(order);
    }

    /**
     * 판매자 주문 목록 조회
     *
     * @param sellerId 판매자 id
     * @param pageable pageable
     * @return OrderInfo
     */
    public PageResponse<OrderInfo> getOrdersBySeller(UUID sellerId, Pageable pageable) {

        Pageable sortPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<Order> orders = orderRepository.findBySellerIdAndDeletedIsNull(sellerId, sortPageable);

        Page<OrderInfo> orderInfos = orders.map(OrderInfo::from);
        return PageResponse.from(orderInfos);
    }

    /**
     * 구매자 주문 목록 조회
     *
     * @param memberId 구매자 id
     * @param pageable pageable
     * @return OrderInfo
     */
    public PageResponse<OrderInfo> getOrdersByConsumer(UUID memberId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByMemberIdAndDeletedIsNull(memberId, pageable);

        Page<OrderInfo> orderInfos = orders.map(OrderInfo::from);
        return PageResponse.from(orderInfos);
    }

    /**
     * 주문 상태 변경
     *
     * @param groupPurchaseId 공동구매 id
     * @param orderStatus     주문 상태
     */
    @Transactional
    public void updateOrderStatus(UUID groupPurchaseId, OrderStatus orderStatus) {
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(groupPurchaseId);

        log.info("orderStatus : {}", orderStatus);

        for (Order order : orders) {

            log.info("orderId : {}", order.getOrderId());
            order.updateStatus(orderStatus);
        }

        orderRepository.saveAll(orders);
    }

    /**
     * 주문 환불
     *
     * @param groupPurchaseId
     */
    @ServiceLog
    @Transactional
    public void returnOrder(UUID groupPurchaseId) {
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(groupPurchaseId, OrderStatus.GROUP_PURCHASE_FAIL);

        log.info("환불 대상 주문 조회 완료 : groupPurchaseId = {}, count = {}", groupPurchaseId, orders.size());

        List<Order> toUpdate = new ArrayList<>();

        int success = 0;
        int fail = 0;

        for (Order order : orders) {

            if (order.isReturned()) {
                log.info("이미 환불된 주문 : {}", order.getOrderId());
                continue;
            }
            Long amount = order.getPrice() * order.getQuantity();

            try {
                paymentClient.returnPointsInternal(
                        order.getMemberId(),
                        new PointReturnRequest(
                                UUID.randomUUID(),
                                order.getOrderId(),
                                amount
                        )
                );
                order.markReturned();
                toUpdate.add(order);
                success++;
                log.info("환불 완료 : orderId = {}, memberId = {}, amount = {}", order.getOrderId(), order.getMemberId(), amount);
            } catch (Exception e) {
                fail++;
                log.error("환불 실패 : orderId = {}, memberId = {}, amount = {}", order.getOrderId(), order.getMemberId(), amount);
                // TODO : 환불 실패 처리 필요(재시도 큐? 관리자 알람?)
            }
        }

        if (!toUpdate.isEmpty()) {
            orderRepository.saveAll(toUpdate);
            log.info("환불 상태 업데이트 완료 : 성공 = {}, 실패 = {}", success, fail);
        }
    }
}
