package store._0982.recommendation.infrastructure.feign.commerce;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import store._0982.common.dto.PageResponse;
import store._0982.recommendation.infrastructure.feign.commerce.dto.ProductPageResponse;

@FeignClient(
        name = "commerce-service",
        url = "${client.commerce}"
)
public interface CommerceProductFeignClient {

    @GetMapping("/internal/products/vector")
    PageResponse<ProductPageResponse> fetchEmbeddingPage(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
