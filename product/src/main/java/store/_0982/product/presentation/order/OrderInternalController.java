package store._0982.product.presentation.order;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.product.application.order.OrderService;
import store._0982.product.application.order.dto.OrderDetailInfo;
import store._0982.product.application.order.dto.OrderInfo;
import store._0982.product.domain.order.OrderStatus;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/internal/orders")
@Hidden
public class OrderInternalController {

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

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    OrderDetailInfo getOrder(
            @PathVariable UUID id,
            @RequestHeader(HeaderName.ID) UUID memberId
    ){
        return orderService.getOrderById(memberId, id);
    }
}
