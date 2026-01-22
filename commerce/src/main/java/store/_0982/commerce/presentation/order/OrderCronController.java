package store._0982.commerce.presentation.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import store._0982.commerce.application.order.OrderService;
import store._0982.common.dto.ResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderCronController {

    private final OrderService orderService;

    @PostMapping("/cancel/retry")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<Void> retryCancelOrder() {
        orderService.retryCancelOrder();
        return new ResponseDto<>(HttpStatus.OK, null, "주문 취소 재처리를 실행했습니다.");
    }
}
