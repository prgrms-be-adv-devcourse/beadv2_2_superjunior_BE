package store._0982.commerce.presentation.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.order.OrderService;
import store._0982.commerce.application.order.dto.OrderCancelInfo;
import store._0982.commerce.presentation.order.dto.OrderCancelRequest;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;

import java.util.UUID;

@Tag(name = "Order", description = "주문 관련 정보")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/orders/cancel")
public class CanceledOrderController {

    private final OrderService orderService;

    @Operation(summary = "주문 취소", description = "주문을 취소합니다.")
    @PostMapping("/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<Void> cancelOrder(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PathVariable UUID orderId,
            @RequestBody OrderCancelRequest orderCancelRequest
            ) {
        orderService.cancelOrder(orderCancelRequest.toCommand(memberId, orderId));
        return new ResponseDto<>(HttpStatus.OK, null, "주문 취소 되었습니다.");
    }

    @Operation(summary = "주문 취소 내역 조회", description = "주문 취소 내역을 페이징하여 조회합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponse<OrderCancelInfo>> getCanceledOrders(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<OrderCancelInfo> response = orderService.getCanceledOrders(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, response, "주문 취소 목록을 조회했습니다.");
    }

    @Operation(summary = "주문 취소 승인", description = "주문 취소 요청에 대해 승인 처리합니다.")
    @PatchMapping("/{orderId}/approve")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<OrderCancelInfo> approvePendingOrder(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PathVariable UUID orderId
    ) {
        OrderCancelInfo info = orderService.approvePendingOrder(memberId, orderId);
        return new ResponseDto<>(HttpStatus.OK, info, "판매자가 주문 취소 승인했습니다.");
    }

    @Operation(summary = "주문 취소 거부", description = "주문 취소 요청에 대해 거부 처리합니다.")
    @PatchMapping("/{orderId}/reject")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<OrderCancelInfo> rejectPendingOrder(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PathVariable UUID orderId
    ) {
        OrderCancelInfo info = orderService.rejectPendingOrder(memberId, orderId);
        return new ResponseDto<>(HttpStatus.OK, info, "판매자가 주문 취소 거부했습니다.");
    }

    @Operation(summary = "주문 취소 대기 내역 조회", description = "주문 취소 대기 내역을 페이징하여 조회합니다.")
    @GetMapping("/pending")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponse<OrderCancelInfo>> getPendingOrder(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<OrderCancelInfo> info = orderService.getPendingOrder(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, info, "주문 취소 대기 내역을 페이징하여 조회했습니다.");
    }
}
