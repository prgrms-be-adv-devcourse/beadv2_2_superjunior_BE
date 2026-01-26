package store._0982.commerce.application.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.cart.dto.CartAddCommand;
import store._0982.commerce.application.cart.dto.CartDeleteCommand;
import store._0982.commerce.application.cart.dto.CartInfo;
import store._0982.commerce.application.cart.dto.CartUpdateCommand;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseDetailInfo;
import store._0982.commerce.application.product.dto.CartVectorInfo;
import store._0982.commerce.domain.cart.Cart;
import store._0982.commerce.domain.cart.CartRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.domain.product.ProductVector;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.product.ProductVectorJpaRepository;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;

    private final GroupPurchaseService groupPurchaseService;
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductVectorJpaRepository productVectorRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartInfo addIntoCart(CartAddCommand command) {
        Cart cart = cartRepository.findByMemberIdAndGroupPurchaseId(command.memberId(), command.groupPurchaseId()).orElse(Cart.create(command.memberId(), command.groupPurchaseId()));
        GroupPurchaseDetailInfo groupPurchaseDetailInfo = groupPurchaseService.getGroupPurchaseById(cart.getGroupPurchaseId());
        if(groupPurchaseDetailInfo.status() != GroupPurchaseStatus.OPEN)
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_NOT_AVAILABLE);
        cart.add(command.quantity());
        return CartInfo.from(cartRepository.save(cart));
    }

    @Transactional
    public void deleteFromCart(CartDeleteCommand command) {
        Cart cart = cartRepository.findById(command.cartId()).orElseThrow(() -> new CustomException(CustomErrorCode.CART_NOT_FOUND));
        checkOwner(cart, command.memberId());
        cart.delete();
    }

    @Transactional
    public CartInfo updateNumOfGpInCart(CartUpdateCommand command) {
        Cart cart = cartRepository.findById(command.cartId()).orElseThrow(() -> new CustomException(CustomErrorCode.CART_NOT_FOUND));
        checkOwner(cart, command.memberId());
        cart.changeQuantity(command.quantity());
        return CartInfo.from(cart);
    }

    public PageResponse<CartInfo> getCarts(UUID memberId, Pageable pageable) {
        Page<CartInfo> cartPage = cartRepository.findAllByMemberId(memberId, pageable).map(CartInfo::from);
        return new PageResponse<>(cartPage.getContent(), cartPage.getTotalPages(), cartPage.getTotalElements(), cartPage.isFirst(), cartPage.isLast(), cartPage.getSize(), cartPage.getNumberOfElements());
    }

    public List<Cart> getCarts(UUID memberId){
        return cartRepository.findAllByMemberId(memberId);
    }

    @Transactional
    public void flushCart(UUID memberId) {
        cartRepository.flushCart(memberId);
    }

    @Transactional
    @ServiceLog
    public void cleanUpZeroCarts() {
        cartRepository.deleteAllZeroQuantity();
    }

    private void checkOwner(Cart cart, UUID memberId) {
        if (!cart.getMemberId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.NOT_CART_OWNER);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteCartById(List<Cart> carts){
        List<UUID> deleteIds = carts.stream()
                .map(Cart::getCartId)
                .toList();
        cartRepository.deleteAllById(deleteIds);
    }

    public List<Cart> validateAndGetCartForOrder(UUID memberId, List<UUID> cartIds){
        List<Cart> carts = cartRepository.findAllByCartIdIn(cartIds);

        if(carts.size() != cartIds.size()){
            throw new CustomException(CustomErrorCode.CART_NOT_FOUND);
        }

        carts.forEach(cart -> {
            if(!cart.getMemberId().equals(memberId)){
                throw new CustomException(CustomErrorCode.NOT_CART_OWNER);
            }

            if(cart.getQuantity() <= 0){
                throw new CustomException(CustomErrorCode.CART_IS_EMPTY);
            }
        });

        return carts;
    }

    public List<CartVectorInfo> getCartVector(UUID memberId) {
        List<Cart> carts = cartRepository.findAllByMemberId(memberId);
        List<UUID> groupPurchaseIds = carts.stream()
                .map(Cart::getGroupPurchaseId)
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
        List<Product> products = productRepository.findByProductIdIn(productIds);
        Map<UUID, String> productIdToDescription = products.stream()
                .collect(toMap(Product::getProductId, Product::getDescription));

        return carts.stream()
                .map(cart -> {
                    UUID productId = groupPurchaseToProduct.get(cart.getGroupPurchaseId());
                    ProductVector vector = productIdToVector.get(productId);
                    String description = productIdToDescription.get(productId);
                    float[] productVector = vector == null ? null : vector.getVector();
                    return new CartVectorInfo(
                            cart.getCartId(),
                            cart.getMemberId(),
                            productId,
                            description,
                            cart.getQuantity(),
                            cart.getCreatedAt(),
                            cart.getUpdatedAt(),
                            productVector
                    );
                })
                .toList();
    }
}
