package store._0982.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import store._0982.order.client.dto.GroupPurchaseInternalInfo;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "product-service", url = "${gateway.host}")
public interface ProductFeignClient {

    @GetMapping("/internal/purchases/unsettled")
    List<GroupPurchaseInternalInfo> getUnSettledGroupPurchase();

    @PutMapping("/internal/purchases/{groupPurchaseId}/settle")
    void markAsSettled(@PathVariable("groupPurchaseId") UUID groupPurchaseId);

}
