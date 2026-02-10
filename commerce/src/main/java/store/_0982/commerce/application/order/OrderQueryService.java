package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.order.dto.OrderCancelInfo;
import store._0982.commerce.application.order.dto.OrderDetailInfo;
import store._0982.commerce.application.order.dto.OrderInfo;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.CanceledOrder;
import store._0982.commerce.domain.order.CanceledOrderRepository;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final CanceledOrderRepository canceledOrderRepository;

    private final GroupPurchaseService groupPurchaseService;


    public OrderDetailInfo getOrderById(UUID requesterID, UUID orderId) {
        Order order = orderRepository.findByOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.ORDER_NOT_FOUND));
        if (!order.getMemberId().equals(requesterID)
                && !order.getSellerId().equals(requesterID)) {
            throw new CustomException(CustomErrorCode.ORDER_ACCESS_DENIED);
        }

        return OrderDetailInfo.from(order);
    }

    public PageResponse<OrderInfo> getOrdersBySeller(UUID sellerId, Pageable pageable) {

        Pageable sortPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Page<Order> orders = orderRepository.findBySellerIdAndDeletedIsNull(sellerId, sortPageable);

        Map<UUID, String> groupPurchaseName = getGroupPurchaseNames(orders);

        Page<OrderInfo> orderInfos = orders.map(order ->
                OrderInfo.from(order, order.getGroupPurchaseId(), groupPurchaseName.get(order.getGroupPurchaseId())));

        return PageResponse.from(orderInfos);
    }

    public PageResponse<OrderInfo> getOrdersByConsumer(UUID memberId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByMemberIdAndDeletedIsNull(memberId, pageable);


        Map<UUID, String> groupPurchaseName = getGroupPurchaseNames(orders);

        Page<OrderInfo> orderInfos = orders.map(order ->
                OrderInfo.from(order, order.getGroupPurchaseId(), groupPurchaseName.get(order.getGroupPurchaseId())));

        return PageResponse.from(orderInfos);
    }

    public List<Order> getAllOrderByMemberId(UUID memberId) {
        return orderRepository.findAllByMemberId(memberId);
    }

    public List<UUID> getGroupPurchaseParticipants(UUID groupPurchaseId) {
        return orderRepository.findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(groupPurchaseId, OrderStatus.participantStatuses());
    }


    public PageResponse<OrderCancelInfo> getCanceledOrders(UUID memberId, Pageable pageable) {
        Page<CanceledOrder> canceledOrders = canceledOrderRepository.findAllByMemberId(memberId, pageable);
        Page<OrderCancelInfo> cancelInfos = canceledOrders.map(OrderCancelInfo::toOrderCancelInfo);
        return PageResponse.from(cancelInfos);
    }

    public PageResponse<OrderCancelInfo> getPendingOrder(UUID sellerId, Pageable pageable) {
        Page<CanceledOrder> canceledOrders = canceledOrderRepository.findAllBySellerIdAndStatus(sellerId, CancelStatus.PENDING, pageable);
        Page<OrderCancelInfo> cancelInfos = canceledOrders.map(OrderCancelInfo::toOrderCancelInfo);
        return PageResponse.from(cancelInfos);
    }
  
    private Map<UUID, String> getGroupPurchaseNames(Page<Order> orders){
      List<UUID> groupPurchaseIds = orders.getContent().stream()
              .map(Order::getGroupPurchaseId)
              .distinct()
              .toList();

      return groupPurchaseService
              .getGroupPurchaseByIds(groupPurchaseIds).stream()
              .collect(Collectors.toMap(
                      GroupPurchase::getGroupPurchaseId,
                      GroupPurchase::getTitle
              ));
    }
}
