package store._0982.order.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import store._0982.order.infrastructure.client.dto.GroupPurchaseInfo;

import java.util.List;

@FeignClient(name = "product-service", url = "${feign.product.url}")
public interface ProductFeignClient {

    @GetMapping("/api/purchases/unsettled")
    List<GroupPurchaseInfo> getUnSettledGroupPurchase();

}
