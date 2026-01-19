package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.order.dto.OrderDetailInfo;
import store._0982.commerce.application.order.dto.OrderInfo;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;

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

        Page<OrderInfo> orderInfos = orders.map(OrderInfo::from);
        return PageResponse.from(orderInfos);
    }

    public PageResponse<OrderInfo> getOrdersByConsumer(UUID memberId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByMemberIdAndDeletedIsNull(memberId, pageable);

        Page<OrderInfo> orderInfos = orders.map(OrderInfo::from);
        return PageResponse.from(orderInfos);
    }

    public List<Order> getAllOrderByMemberId(UUID memberId) {
        return orderRepository.findAllByMemberId(memberId);
    }
}
