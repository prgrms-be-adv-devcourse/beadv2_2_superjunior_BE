package store._0982.order.presentation;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.order.application.OrderService;
import store._0982.order.domain.OrderStatus;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/internal/orders")
@Hidden
public class InternalController {

    private final OrderService orderService;

    @PostMapping("/{purchaseId}/status")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<Void> updateOrderStatus(
            @PathVariable UUID purchaseId,
            @RequestParam("status") String status
    ) {
        orderService.updateOrderStatus(purchaseId, OrderStatus.valueOf(status));
        return new ResponseDto<>(HttpStatus.OK, null, "주문 상태가 변경되었습니다.");
    }

    @PostMapping("/{purchaseId}/return")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<Void> returnOrders(
            @PathVariable UUID purchaseId
    ) {
        orderService.returnOrder(purchaseId);
        return new ResponseDto<>(HttpStatus.OK, null, "포인트가 환불되었습니다.");
    }
}
