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
import store._0982.commerce.application.product.dto.OrderVectorInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.domain.product.ProductVector;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.product.ProductVectorJpaRepository;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductVectorJpaRepository productVectorRepository;


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

    public List<OrderVectorInfo> getOrderVector(UUID memberId) {
        List<Order> orders = orderRepository.findAllByMemberId(memberId);
        List<UUID> groupPurchaseIds = orders.stream()
                .map(Order::getGroupPurchaseId)
                .toList();
        List<GroupPurchase> groupPurchases = groupPurchaseRepository.findAllByGroupPurchaseIdIn(groupPurchaseIds);
        List<UUID> productIds = groupPurchases.stream()
                .map(GroupPurchase::getProductId)
                .toList();
        List<ProductVector> productVectors = productVectorRepository.findByProductIdIn(productIds);
        Map<UUID, UUID> groupPurchaseToProduct = groupPurchases.stream()
                .collect(toMap(GroupPurchase::getGroupPurchaseId, GroupPurchase::getProductId));
        Map<UUID, ProductVector> productIdToVector = productVectors.stream()
                .collect(toMap(ProductVector::getProductId, Function.identity()));
        return orders.stream()
                .map(order -> {
                    UUID productId = groupPurchaseToProduct.get(order.getGroupPurchaseId());
                    ProductVector vector = productIdToVector.get(productId);
                    float[] productVector = vector == null ? null : vector.getVector();
                    return new OrderVectorInfo(
                            order.getOrderId(),
                            order.getMemberId(),
                            productId,
                            order.getQuantity(),
                            order.getCreatedAt(),
                            order.getStatus(),
                            productVector
                    );
                })
                .toList();
    }

    public List<UUID> getGroupPurchaseParticipants(UUID groupPurchaseId) {
        return orderRepository.findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(groupPurchaseId, OrderStatus.participantStatuses());
    }
}
