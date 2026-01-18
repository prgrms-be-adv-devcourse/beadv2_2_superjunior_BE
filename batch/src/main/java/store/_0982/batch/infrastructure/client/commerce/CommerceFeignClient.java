package store._0982.batch.infrastructure.client.commerce;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.batch.domain.ai.CartVector;
import store._0982.batch.domain.ai.OrderVector;
import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "commerce-service",
        url = "${client.commerce:http://localhost:8087}",
        configuration = FeignConfig.class
)
public interface CommerceFeignClient {

    @GetMapping(value = "/api/orders/consumer")
    ResponseDto<List<OrderVector>> getOrdersConsumer(@RequestHeader("X-Member-Id") UUID memberId);

    @GetMapping(value = "/api/carts")
    ResponseDto<List<CartVector>> getCarts(@RequestHeader("X-Member-Id") UUID memberId);
}
