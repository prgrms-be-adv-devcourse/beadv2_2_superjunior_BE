package store._0982.order.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
import store._0982.order.application.OrderService;
import store._0982.order.application.dto.OrderDetailInfo;
import store._0982.order.application.dto.OrderInfo;
import store._0982.order.application.dto.OrderRegisterInfo;
import store._0982.order.presentation.dto.OrderRegisterRequest;

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
            @RequestHeader("X-Member-Id") UUID memberId,
            @Valid @RequestBody OrderRegisterRequest request) {
        OrderRegisterInfo response = orderService.createOrder(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, response, "주문이 생성되었습니다.");
    }

    @Operation(summary = "주문 조회", description = "주문을 조회합니다.")
    @GetMapping("/{orderId}")
    @RequireRole({Role.CONSUMER, Role.SELLER, Role.ADMIN})
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<OrderDetailInfo> getOrderById(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable UUID orderId) {
        OrderDetailInfo response = orderService.getOrderById(memberId, orderId);
        return new ResponseDto<>(HttpStatus.OK, response, "주문 상세 조회가 완료 되었습니다.");
    }

    @Operation(summary = "주문 목록 조회", description = "주문 목록을 조회합니다.")
    @GetMapping
    @RequireRole({Role.CONSUMER, Role.SELLER, Role.ADMIN})
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponse<OrderInfo>> getOrders(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestHeader(HeaderName.ROLE) String roleH,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Role role = Role.valueOf(roleH.toUpperCase());
        PageResponse<OrderInfo> response = null;
        if(role == Role.SELLER){
            response = orderService.getOrdersBySeller(memberId, pageable);
        }else{
            response = orderService.getOrdersByConsumer(memberId, pageable);
        }
        return new ResponseDto<>(HttpStatus.OK, response, "주문 목록 조회가 완료 되었습니다.");
    }

}
