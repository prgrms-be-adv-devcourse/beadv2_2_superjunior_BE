package store._0982.order.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import store._0982.order.infrastructure.client.dto.GroupPurchaseInternalInfo;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product-service", url = "${feign.product.url}")
public interface ProductFeignClient {

    @GetMapping("/api/purchases/unsettled")
    List<GroupPurchaseInternalInfo> getUnSettledGroupPurchase();

    @PutMapping("/api/purchases/{groupPurchaseId}/settle")
    void markAsSettled(@PathVariable("groupPurchaseId") UUID groupPurchaseId);

}
