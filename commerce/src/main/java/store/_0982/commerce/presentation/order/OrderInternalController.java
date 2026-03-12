package store._0982.commerce.presentation.order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.order.OrderService;
import store._0982.commerce.application.order.dto.OrderDetailInfo;
import store._0982.common.HeaderName;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/orders")
public class OrderInternalController {

    private final OrderService orderService;

    @GetMapping("/{groupPurchaseId}/participants")
    @ResponseStatus(HttpStatus.OK)
    List<UUID> getGroupPurchaseParticipants(@PathVariable UUID groupPurchaseId){
        return orderService.getGroupPurchaseParticipants(groupPurchaseId);
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