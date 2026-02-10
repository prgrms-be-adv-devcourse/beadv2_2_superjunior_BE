package store._0982.recommendation.infrastructure.feign.commerce;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import store._0982.recommendation.infrastructure.feign.commerce.dto.GroupPurchasePerformanceInfo;
import store._0982.recommendation.infrastructure.feign.commerce.dto.OpenGroupPurchaseInfo;
import store._0982.recommendation.infrastructure.feign.commerce.dto.ProductDetailInfo;

import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "commerce-service",
        url = "${client.commerce}"
)
public interface CommerceFeignClient {
    @GetMapping("/internal/products/{productId}")
    ProductDetailInfo getProduct(@PathVariable UUID productId);

    @PostMapping("/internal/purchases/performance")
    List<GroupPurchasePerformanceInfo> getPerformance(
            @RequestBody List<UUID> groupPurchaseIds
    );

    @GetMapping("/internal/purchases/open")
    List<OpenGroupPurchaseInfo> getOpenGroupPurchases(
            @RequestParam(value = "limit", defaultValue = "200") int limit
    );
}