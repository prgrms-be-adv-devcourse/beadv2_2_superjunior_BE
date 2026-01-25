package store._0982.commerce.presentation.order;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.order.OrderService;
import store._0982.commerce.application.order.dto.OrderCancelInfo;
import store._0982.commerce.application.order.dto.OrderDetailInfo;
import store._0982.commerce.application.order.dto.OrderInfo;
import store._0982.commerce.application.order.dto.OrderRegisterInfo;
import store._0982.commerce.presentation.order.dto.OrderCancelRequest;
import store._0982.commerce.presentation.order.dto.OrderCartRegisterRequest;
import store._0982.commerce.presentation.order.dto.OrderRegisterRequest;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

@Tag(name = "Order", description = "주문 관련 정보")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 생성", description = "주문을 생성합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<OrderRegisterInfo> createOrder(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @Valid @RequestBody OrderRegisterRequest request) {
        OrderRegisterInfo response = orderService.createOrder(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, response, "주문이 생성되었습니다.");
    }

    @Operation(summary = "장바구니에서 주문 생성", description = "장바구니에서 주문을 생성합니다.")
    @PostMapping("/cart")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<List<OrderRegisterInfo>> createOrderCart(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @Valid @RequestBody OrderCartRegisterRequest request) {
        List<OrderRegisterInfo> response = orderService.createOrderCart(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, response, "장바구니 주문이 생성되었습니다.");
    }

    @Operation(summary = "주문 조회", description = "주문을 조회합니다.")
    @GetMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<OrderDetailInfo> getOrderById(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PathVariable UUID orderId) {
        OrderDetailInfo response = orderService.getOrderById(memberId, orderId);
        return new ResponseDto<>(HttpStatus.OK, response, "주문 상세 조회가 완료 되었습니다.");
    }


    @Operation(summary = "주문 목록 조회(구매자)", description = "주문 목록(구매자)을 조회합니다.")
    @GetMapping("/consumer")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponse<OrderInfo>> getOrdersConsumer(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )Pageable pageable) {

        PageResponse<OrderInfo> response = orderService.getOrdersByConsumer(memberId, pageable);

        return new ResponseDto<>(HttpStatus.OK, response, "주문 목록 조회(구매자)가 완료 되었습니다.");
    }

    // TODO : 판매자 공동 구매별 주문 요약 -> 클릭 시 공동 구매별 주문 목록 조회 -> 목록 클릭 시 주문 상세 조회 필요(BFF)
    @Operation(summary = "주문 목록 조회(판매자)", description = "주문 목록(판매자)을 조회합니다.")
    @GetMapping("/seller")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponse<OrderInfo>> getOrdersSeller(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.ASC
            )Pageable pageable) {

        PageResponse<OrderInfo> response = orderService.getOrdersBySeller(memberId, pageable);

        return new ResponseDto<>(HttpStatus.OK, response, "주문 목록 조회(판매자)가 완료 되었습니다.");
    }

    @PostMapping("/cancel/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<Void> cancelOrder(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PathVariable UUID orderId,
            @RequestBody OrderCancelRequest orderCancelRequest
            ) {
        orderService.cancelOrder(orderCancelRequest.toCommand(memberId, orderId));
        return new ResponseDto<>(HttpStatus.OK, null, "주문 취소 되었습니다.");
    }

    @GetMapping("/cancel")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponse<OrderCancelInfo>> getCanceledOrders(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<OrderCancelInfo> response = orderService.getCanceledOrders(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, response, "주문 취소 목록을 조회했습니다.");
    }

    @PatchMapping("/{orderId}/purchase-confirmed")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<Void> confirmPurchase(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PathVariable UUID orderId
    ){
        orderService.confirmPurchase(memberId, orderId);
        return new ResponseDto<>(HttpStatus.OK, null, "구매 확정되었습니다.");
    }
}
