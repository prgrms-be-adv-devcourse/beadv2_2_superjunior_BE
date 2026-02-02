package store._0982.commerce.application.product;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.cart.CartService;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.order.OrderService;
import store._0982.commerce.application.product.dto.CartVectorInfo;
import store._0982.commerce.application.product.dto.OrderVectorInfo;
import store._0982.commerce.application.product.dto.ProductEmbeddingCompleteInfo;
import store._0982.commerce.application.product.event.VectorCollectedEvent;
import store._0982.commerce.domain.cart.Cart;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.product.ProductVector;
import store._0982.commerce.infrastructure.product.ProductVectorJpaRepository;

import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;
import store._0982.common.log.ServiceLog;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductEmbeddingService {

    private final ProductVectorJpaRepository vectorizeRepository; //DDD interface 해야할 것 같습니다.
    private final ApplicationEventPublisher eventPublisher;
    private final OrderService orderService;
    private final CartService cartService;
    private final GroupPurchaseService groupPurchaseService;

    @Value("${spring.ai.openai.embedding.options.model}")
    private String currentModelVersion;

    @ServiceLog
    @Transactional
    public ProductEmbeddingCompleteInfo updateEmbedding(ProductEmbeddingCompletedEvent completeEvent) {
        ProductVector vector = new ProductVector(completeEvent, currentModelVersion);
        ProductVector saved = vectorizeRepository.save(vector);
        return ProductEmbeddingCompleteInfo.from(saved);
    }

    public void collectVector(UUID memberId) {
        List<Order> orders = orderService.getAllOrderByMemberId(memberId).stream().limit(100).toList();
        List<Cart> carts = cartService.getCarts(memberId).stream().limit(100).toList();

        List<GroupPurchase> orderGroupPurchases = groupPurchaseService.getGroupPurchaseByIds(orders.stream().map(Order::getGroupPurchaseId).toList());
        List<GroupPurchase> cartGroupPurchases = groupPurchaseService.getGroupPurchaseByIds(carts.stream().map(Cart::getGroupPurchaseId).toList());

        List<ProductVector> orderProductVectors = getProductVectors(orderGroupPurchases.stream().map(GroupPurchase::getProductId).toList());
        List<ProductVector> cartProductVectors = getProductVectors(cartGroupPurchases.stream().map(GroupPurchase::getProductId).toList());

        List<OrderVectorInfo> orderVectorInfos = generateOrderVectors(orders, orderGroupPurchases, orderProductVectors);
        List<CartVectorInfo> cartVectorInfos = generateCartVectors(carts, cartGroupPurchases, cartProductVectors);
        VectorCollectedEvent vectorCollectedEvent = new VectorCollectedEvent(orderVectorInfos, cartVectorInfos);

        //todo: kafka 통신



    }

    private List<ProductVector> getProductVectors(List<UUID> productIds) {
        return vectorizeRepository.findByProductIdIn(productIds);
    }

    private List<OrderVectorInfo> generateOrderVectors(List<Order> orders, List<GroupPurchase> groupPurchases, List<ProductVector> productVectorInfos) {
        Map<UUID, UUID> groupPurchaseToProduct = groupPurchases.stream()
                .collect(toMap(GroupPurchase::getGroupPurchaseId, GroupPurchase::getProductId));
        Map<UUID, ProductVector> productIdToVector = productVectorInfos.stream()
                .collect(toMap(ProductVector::getProductId, Function.identity()));

        return orders.stream()
                .map(order -> {
                    UUID productId = groupPurchaseToProduct.get(order.getGroupPurchaseId());
                    ProductVector vector = productIdToVector.get(productId);
                    return new OrderVectorInfo(
                            order.getOrderId(),
                            order.getMemberId(),
                            productId,
                            null,
                            order.getQuantity(),
                            order.getCreatedAt(),
                            order.getStatus(),
                            vector.getVector()
                    );
                })
                .toList();
    }

    private List<CartVectorInfo> generateCartVectors(List<Cart> carts, List<GroupPurchase> groupPurchases, List<ProductVector> productVectorInfos) {
        Map<UUID, UUID> groupPurchaseToProduct = groupPurchases.stream()
                .collect(toMap(GroupPurchase::getGroupPurchaseId, GroupPurchase::getProductId));
        Map<UUID, ProductVector> productIdToVector = productVectorInfos.stream()
                .collect(toMap(ProductVector::getProductId, Function.identity()));

        return carts.stream()
                .map(cart -> {
                    UUID productId = groupPurchaseToProduct.get(cart.getGroupPurchaseId());
                    ProductVector vector = productIdToVector.get(productId);
                    return new CartVectorInfo(
                            cart.getCartId(),
                            cart.getMemberId(),
                            productId,
                            null,
                            cart.getQuantity(),
                            cart.getCreatedAt(),
                            cart.getUpdatedAt(),
                            vector.getVector()
                    );
                })
                .toList();
    }
}
