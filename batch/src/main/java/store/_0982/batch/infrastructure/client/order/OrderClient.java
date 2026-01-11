package store._0982.batch.infrastructure.client.order;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import store._0982.common.dto.ResponseDto;

import java.util.UUID;

@FeignClient(
        name="order",
        url = "${gateway.host}"
)
public interface OrderClient {

    @PostMapping("/internal/orders/{purchaseId}/status")
    ResponseDto<Void> updateOrderStatus(
            @PathVariable("purchaseId") UUID purchaseId,
            @RequestParam("status") String status
    );

    @PostMapping("/internal/orders/{purchaseId}/return")
    ResponseDto<Void> returnOrders(
            @PathVariable("purchaseId") UUID purchaseId
    );
}
