package store._0982.commerce.presentation.cart.dto;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.cart.CartService;
import store._0982.commerce.application.product.dto.CartVectorInfo;
import store._0982.common.HeaderName;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/carts")
public class CartInternalController {
    private final CartService cartService;

    @Operation(summary = "장바구니 상품 벡터 조회")
    @GetMapping
    public List<CartVectorInfo> getCarts(@RequestHeader(value = HeaderName.ID) UUID memberId) {
        return cartService.getCartVector(memberId);
    }

}
