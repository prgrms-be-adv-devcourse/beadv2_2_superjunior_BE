package store._0982.product.batch.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/api/orders/internal/{purchaseId}/status")
    ResponseDto<Void> updateOrderStatus(
            @PathVariable("purchaseId") UUID purchaseId,
            @RequestParam("status") String status
    );

    @PostMapping("/api/orders/internal/{purchaseId}/return")
    ResponseDto<Void> returnOrders(
            @PathVariable("purchaseId") UUID purchaseId
    );
}
