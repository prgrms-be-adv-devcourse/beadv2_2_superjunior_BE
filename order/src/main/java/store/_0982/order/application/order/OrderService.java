package store._0982.order.application.order;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.order.application.order.dto.*;
import store._0982.order.domain.cart.Cart;
import store._0982.order.domain.cart.CartRepository;
import store._0982.order.domain.order.Order;
import store._0982.order.domain.order.OrderRepository;
import store._0982.order.domain.order.OrderStatus;
import store._0982.order.exception.CustomErrorCode;
import store._0982.order.infrastructure.client.member.MemberClient;
import store._0982.order.infrastructure.client.payment.PaymentClient;
import store._0982.order.infrastructure.client.payment.dto.PointDeductRequest;
import store._0982.order.infrastructure.client.payment.dto.PointReturnRequest;
import store._0982.order.infrastructure.client.product.ProductClient;
import store._0982.order.infrastructure.client.product.dto.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final MemberClient memberClient;
    private final ProductClient productClient;
    private final PaymentClient paymentClient;

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

        // 주문자 존재 여부
        validateMember(memberId);

        // 공동 구매 존재 여부
        GroupPurchaseDetailInfo purchase = validateGroupPurchase(command.groupPurchaseId());

        // order 생성
        Order order = new Order(
                command.quantity(),
                purchase.discountedPrice(),
                memberId,
                command.address(),
                command.addressDetail(),
                command.postalCode(),
                command.receiverName(),
                command.sellerId(),
                command.groupPurchaseId()
        );

        Order savedOrder = orderRepository.save(order);

        deductPoints(memberId, savedOrder.getOrderId(), command, command.quantity() * purchase.discountedPrice());
        participate(command);
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
        validateMember(memberId);

        // cartId 리스트로 장바구니 아이템들 조회
        List<Cart> carts = cartRepository.findAllByCartIdIn(command.cartIds());
        if (carts.size() != command.cartIds().size()) {
            throw new CustomException(CustomErrorCode.CART_NOT_FOUND);
        }

        // 장바구니가 해당 회원 것인지 확인
        carts.forEach(cart -> {
            if (!cart.getMemberId().equals(memberId)) {
                throw new CustomException(CustomErrorCode.NOT_CART_OWNER);
            }

            if (cart.getQuantity() < 0) {
                throw new CustomException(CustomErrorCode.INVALID_QUANTITY);
            }
        });

        // 공동 구매 유효한지 확인 -> 참여 -> 포인트 차감
        Set<UUID> groupPurchaseIds = carts.stream()
                .map(Cart::getGroupPurchaseId)
                .collect(Collectors.toSet());

        // 공동 구매 리스트 조회
        ResponseDto<List<GroupPurchaseInfo>> response = productClient.getGroupPurchaseByIds(new ArrayList<>(groupPurchaseIds));

        List<GroupPurchaseInfo> purchases = response.data();

        Map<UUID, GroupPurchaseInfo> purchasesMap = purchases.stream()
                .collect(Collectors.toMap(
                        GroupPurchaseInfo::groupPurchaseId,
                        Function.identity()
                ));

        // 각 공동 구매가 조건에 맞는지 확인
        purchasesMap.values().forEach(purchase -> {
            if (purchase.status() != GroupPurchaseStatus.OPEN) {
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_NOT_OPEN);
            }
            if (purchase.endDate().isBefore(OffsetDateTime.now())) {
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_END);
            }
        });

        // 주문 생성
        List<OrderRegisterInfo> results = new ArrayList<>();

        for (Cart cart : carts) {
            GroupPurchaseInfo purchase = purchasesMap.get(cart.getGroupPurchaseId());

            if (purchase == null) {
                throw new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND);
            }

            // 공동 구매 참여
            OrderRegisterCommand orderCommand = new OrderRegisterCommand(
                    cart.getQuantity(),
                    command.address(),
                    command.addressDetail(),
                    command.postalCode(),
                    command.receiverName(),
                    purchase.sellerId(),
                    purchase.groupPurchaseId()
            );

            // Order 생성
            Order order = new Order(
                    cart.getQuantity(),
                    purchase.discountedPrice(),
                    memberId,
                    command.address(),
                    command.addressDetail(),
                    command.postalCode(),
                    command.receiverName(),
                    purchase.sellerId(),
                    cart.getGroupPurchaseId()
            );

            Order savedOrder = orderRepository.save(order);

            deductPoints(memberId, savedOrder.getOrderId(), orderCommand, cart.getQuantity() * purchase.discountedPrice());
            participate(orderCommand);
            results.add(OrderRegisterInfo.from(savedOrder));
        }

        // 장바구니 비우기
        cartRepository.deleteAll(carts);

        return results;
    }


    private void validateMember(UUID memberId) {
        try {
            memberClient.getProfile(memberId);
        } catch (FeignException.NotFound e) {
            throw new CustomException(CustomErrorCode.MEMBER_NOT_FOUND);
        } catch (FeignException e) {
            log.error("회원 조회 실패 : memberId={}, status={}", memberId, e.status());
            throw new RuntimeException("회원 정보 조회 오류");
        }
    }

    private GroupPurchaseDetailInfo validateGroupPurchase(UUID groupPurchaseId) {
        try {
            // ResponseDto로 받기
            GroupPurchaseDetailInfo groupPurchase =
                    productClient.getGroupPurchaseById(groupPurchaseId).data();

            // 상태확인
            if (groupPurchase.status() != GroupPurchaseStatus.OPEN) {
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_NOT_OPEN);
            }

            // 종료시간 확인
            if (groupPurchase.endDate().isBefore(OffsetDateTime.now())) {
                throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_END);
            }
            return groupPurchase;

        } catch (CustomException e) {
            throw e;

        } catch (FeignException.NotFound e) {
            log.error("공동구매 없음: groupPurchaseId={}", groupPurchaseId);
            throw new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND);

        } catch (FeignException e) {
            log.error("공동구매 조회 실패: groupPurchaseId={}, message={}", groupPurchaseId, e.getMessage());
            throw new RuntimeException("공동 구매 정보 조회 오류 발생");
        }
    }

    private void participate(OrderRegisterCommand command) {
        // 공동 구매 참여
        ParticipateRequest participateRequest = new ParticipateRequest(command.quantity());
        ParticipateInfo participateInfo = productClient.participate(
                command.groupPurchaseId(),
                participateRequest
        ).data();

        if (!participateInfo.success()) {
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_REACHED);
        }

    }

    private void deductPoints(UUID memberId, UUID orderId, OrderRegisterCommand command, Long price) {
        Long totalAmount = price * command.quantity();
        log.info("가격 : {}, 수량 : {}, 총 가격 : {}", price, command.quantity(), totalAmount);
        try {
            paymentClient.deductPointsInternal(
                    memberId,
                    new PointDeductRequest(UUID.randomUUID(), orderId, totalAmount)
            );
        } catch (FeignException.BadRequest e) {
            log.error("포인트 부족 : memberId={}, amount={}", memberId, totalAmount, e);
            throw e;
        } catch (FeignException e) {
            log.error("포인트 차감 실패: memberId={}, amount={}", memberId, totalAmount, e);
            throw e;
        }
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
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(groupPurchaseId, OrderStatus.FAILED);

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
