package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.order.dto.*;
import store._0982.commerce.application.product.dto.OrderVectorInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.product.ProductVector;
import store._0982.commerce.infrastructure.product.ProductVectorJpaRepository;
import store._0982.common.dto.PageResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductVectorJpaRepository productVectorRepository;

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
     * internal orderVector 조회
     *
     * @param memberId
     * @return List<OrderVectorInfo>
     */
    public List<OrderVectorInfo> getOrderVector(UUID memberId) {
        List<Order> orders = orderQueryService.getAllOrderByMemberId(memberId);
        List<UUID> groupPurchaseIds = orders.stream()
                .map(Order::getGroupPurchaseId)
                .toList();
        if (groupPurchaseIds.isEmpty()) {
            return List.of();
        }
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
}
