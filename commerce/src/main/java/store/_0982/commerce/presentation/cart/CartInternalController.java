package store._0982.commerce.presentation.cart;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.cart.CartService;
import store._0982.commerce.application.product.dto.CartVectorInfo;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/carts")
public class CartInternalController {
    private final CartService cartService;

    @Operation(summary = "장바구니 상품 벡터 조회")
    @GetMapping
    public ResponseDto<List<CartVectorInfo>> getCarts(@RequestHeader(value = HeaderName.ID) UUID memberId) {
        return new ResponseDto<>(HttpStatus.OK, cartService.getCartVector(memberId), "카트 벡터 조회 완료");
    }

}
