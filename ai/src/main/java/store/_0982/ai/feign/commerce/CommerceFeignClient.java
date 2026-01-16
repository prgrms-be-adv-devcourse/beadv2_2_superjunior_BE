package store._0982.ai.feign.commerce;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.ai.feign.commerce.dto.CartInfo;
import store._0982.ai.feign.commerce.dto.OrderInfo;
import store._0982.common.dto.ResponseDto;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "commerce-service",
        url = "${member.host.url:http://localhost:8087}"
)
public interface CommerceFeignClient{
    @GetMapping(value="/api/orders/consumer")
    ResponseDto<List<OrderInfo>> getOrdersConsumer(@RequestHeader("X-Member-Id") UUID memberId);

    @GetMapping(value = "/api/carts")
    ResponseDto<List<CartInfo>> getCarts(@RequestHeader("X-Member-Id") UUID memberId);
}
