package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.order.dto.*;
import store._0982.commerce.domain.order.Order;
import store._0982.common.dto.PageResponse;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

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

    /**
     * 주문 취소
     *
     * @param command
     */
    public void cancelOrder(OrderCancelCommand command) {
        orderCommandService.cancelOrder(command);
    }

    /**
     * 모든 주문 조회
     *
     * @param memberId
     * @return
     */
    public List<Order> getAllOrderByMemberId(UUID memberId) {
        return orderQueryService.getAllOrderByMemberId(memberId);
    }

    /**
     * 공동구매 실패한 주문 상태 변경 처리
     *
     * @param groupPurchaseId 주문 UUID
     */
    public void processGroupPurchaseFailure(UUID groupPurchaseId){
        orderCommandService.processGroupPurchaseFailure(groupPurchaseId);
    }

    /**
     * 공동 구매 상태에 따라 주문 상태 변경 처리
     * @param event 이벤트
     */
    public void handleUpdatedGroupPurchase(GroupPurchaseEvent event){
        orderCommandService.handleUpdatedGroupPurchase(event);
    }
}
