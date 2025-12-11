package store._0982.order.presentation.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.order.application.cart.CartService;
import store._0982.order.application.cart.dto.CartInfo;
import store._0982.order.presentation.cart.dto.CartAddRequest;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {
    private final CartService cartService;

    @PostMapping
    public ResponseDto<CartInfo> addIntoCart(@RequestHeader(value = "X-Member-Id", required = false) UUID memberId, @RequestBody CartAddRequest cartAddRequest) {
        CartInfo cartInfo = cartService.addIntoCart(cartAddRequest.toCommand(memberId));
        return new ResponseDto<>(HttpStatus.CREATED, cartInfo, "카트에 공동 구매 상품이 추가되었습니다.");
    }
}
