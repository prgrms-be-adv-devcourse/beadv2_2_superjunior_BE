package store._0982.product.application.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.product.application.order.OrderService;
import store._0982.product.application.cart.dto.CartAddCommand;
import store._0982.product.application.cart.dto.CartDeleteCommand;
import store._0982.product.application.cart.dto.CartInfo;
import store._0982.product.application.cart.dto.CartUpdateCommand;
import store._0982.product.domain.cart.Cart;
import store._0982.product.domain.cart.CartRepository;
import store._0982.product.exception.CustomErrorCode;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;
    private final OrderService orderService;

    @Transactional
    public CartInfo addIntoCart(CartAddCommand command) {
        Cart cart = cartRepository.findByMemberIdAndGroupPurchaseId(command.memberId(), command.groupPurchaseId()).orElse(Cart.create(command.memberId(), command.groupPurchaseId()));
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

}
