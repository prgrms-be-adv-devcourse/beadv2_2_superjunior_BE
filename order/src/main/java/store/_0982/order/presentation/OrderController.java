package store._0982.order.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.order.application.OrderService;
import store._0982.order.application.dto.OrderRegisterInfo;
import store._0982.order.presentation.dto.OrderRegisterRequest;

import java.util.UUID;

@Tag(name="Order", description = "주문 관련 정보")
@RestController
@RequiredArgsConstructor
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
    
}
