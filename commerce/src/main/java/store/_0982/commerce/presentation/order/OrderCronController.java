package store._0982.commerce.presentation.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Order Cron", description = "주문 관련 배치/관리자용 API")
public class OrderCronController {

    private final OrderService orderService;

    @PostMapping("/cancel/retry")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "주문 취소 재처리 트리거", description = "취소 요청 상태의 주문을 재처리하는 배치 트리거 API입니다.")
    public ResponseDto<Void> retryCancelOrder() {
        orderService.retryCancelOrder();
        return new ResponseDto<>(HttpStatus.OK, null, "주문 취소 재처리를 실행했습니다.");
    }

    @PostMapping("/cancel/auto")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "주문 취소 승인 자동 트리거", description = "취소 요청 상태의 주문을 자동으로 승인해주는 배치 트리거 API입니다.")
    public ResponseDto<Void> autoCancelOrder() {
        orderService.autoCancelOrder();
        return new ResponseDto<>(HttpStatus.OK, null, "주문 취소 재처리를 실행했습니다.");
    }

    @PostMapping("/expire")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "주문 만료 처리", description = "만료된 주문에 대해서 참여자 수 감소하는 API입니다.")
    public ResponseDto<Void> expiredOrders() {
        orderService.processExpiredOrders();
        return new ResponseDto<>(HttpStatus.OK, null, "만료된 주문을 처리했습니다.");
    }
}
