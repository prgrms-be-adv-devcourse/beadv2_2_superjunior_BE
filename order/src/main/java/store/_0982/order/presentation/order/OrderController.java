package store._0982.order.presentation.order;


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
import store._0982.common.HeaderName;
import store._0982.common.auth.RequireRole;
import store._0982.common.auth.Role;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.order.application.order.OrderService;
import store._0982.order.application.dto.*;
import store._0982.order.presentation.order.dto.OrderCartRegisterRequest;
import store._0982.order.presentation.order.dto.OrderRegisterRequest;

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
    @RequireRole({Role.CONSUMER, Role.SELLER, Role.ADMIN})
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<OrderDetailInfo> getOrderById(
            @RequestHeader(value = HeaderName.ID) UUID memberId,
            @PathVariable UUID orderId) {
        OrderDetailInfo response = orderService.getOrderById(memberId, orderId);
        return new ResponseDto<>(HttpStatus.OK, response, "주문 상세 조회가 완료 되었습니다.");
    }


    @Operation(summary = "주문 목록 조회(구매자)", description = "주문 목록(구매자)을 조회합니다.")
    @GetMapping("/consumer")
    @RequireRole({Role.CONSUMER, Role.SELLER})
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
    @RequireRole({Role.SELLER})
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

}
