package store._0982.order.presentation.cart;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.order.application.cart.CartService;
import store._0982.order.application.cart.dto.CartInfo;
import store._0982.order.presentation.cart.dto.CartAddRequest;
import store._0982.order.presentation.cart.dto.CartDeleteRequest;
import store._0982.order.presentation.cart.dto.CartUpdateRequest;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {   //TODO: batch로 soft delete한 장바구니 hard delete 필요
    private final CartService cartService;

    @Operation(summary = "장바구니에 공동구매상품 추가")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<CartInfo> addIntoCart(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody CartAddRequest cartAddRequest) {
        CartInfo cartInfo = cartService.addIntoCart(cartAddRequest.toCommand(memberId));
        return new ResponseDto<>(HttpStatus.CREATED, cartInfo, "장바구니에 공동구매상품이 추가되었습니다.");
    }

    @Operation(summary = "장바구니에서 공동구매상품 제거")
    @DeleteMapping
    public ResponseDto<Void> deleteFromCart(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody CartDeleteRequest cartDeleteRequest) {
        cartService.deleteFromCart(cartDeleteRequest.toCommand(memberId));
        return new ResponseDto<>(HttpStatus.OK, null, "장바구니에서 공동구매상품이 제거되었습니다.");
    }

    @Operation(summary = "장바구니 내 공동구매상품 수량 변경")
    @PatchMapping
    public ResponseDto<CartInfo> updateNumOfGpInCart(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody CartUpdateRequest cartUpdateRequest) {
        CartInfo cartInfo = cartService.updateNumOfGpInCart(cartUpdateRequest.toCommand(memberId));
        return new ResponseDto<>(HttpStatus.OK, cartInfo, "장바구니에 담은 공동구매상품의 개수가 변경되었습니다.");
    }

    @Operation(summary = "장바구니 목록 조회")
    @GetMapping
    public ResponseDto<PageResponse<CartInfo>> getCarts(@RequestHeader(value = HeaderName.ID) UUID memberId, Pageable pageable) {
        PageResponse<CartInfo> carts = cartService.getCarts(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, carts, "장바구니에 담긴 공동구매상품 리스트");
    }

    @Operation(summary = "장바구니 비우기")
    @DeleteMapping("/all")
    public ResponseDto<Void> flushCart(@RequestHeader(value = HeaderName.ID) UUID memberId) {
        cartService.flushCart(memberId);
        return new ResponseDto<>(HttpStatus.OK, null, "장바구니를 비웠습니다.");
    }

}
