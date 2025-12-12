package store._0982.point.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import store._0982.common.HeaderName;
import store._0982.point.client.dto.OrderInfo;

import java.util.UUID;

@FeignClient(name = "gateway")
public interface OrderServiceClient {
    @GetMapping("/internal/orders/{id}")
    OrderInfo getOrder(@PathVariable UUID id, @RequestHeader(HeaderName.ID) UUID memberId);
}
