package store._0982.commerce.presentation.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.order.OrderService;
import store._0982.commerce.application.order.dto.OrderDetailInfo;
import store._0982.commerce.application.product.dto.OrderVectorInfo;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/internal/orders")
public class OrderInternalController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    OrderDetailInfo getOrder(
            @PathVariable UUID id,
            @RequestHeader(HeaderName.ID) UUID memberId
    ){
        return orderService.getOrderById(memberId, id);
    }

    @GetMapping("consumer")
    @ResponseStatus(HttpStatus.OK)
    ResponseDto<List<OrderVectorInfo>> getOrdersConsumer(@RequestHeader(HeaderName.ID) UUID memberId){
        return new ResponseDto<>(HttpStatus.OK, orderService.getOrderVector(memberId), "주문 벡터 조회 완료");
    }

    @GetMapping("/{groupPurchaseId}/participants")
    @ResponseStatus(HttpStatus.OK)
    List<UUID> getGroupPurchaseParticipants(@PathVariable UUID groupPurchaseId){
        return orderService.getGroupPurchaseParticipants(groupPurchaseId);
    }
}
