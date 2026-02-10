package store._0982.elasticsearch.infrastructure.client.recommendation;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import store._0982.common.dto.ResponseDto;
import store._0982.elasticsearch.infrastructure.client.recommendation.dto.ProductVectorInfo;

@FeignClient(
        name = "recommendation-service",
        url = "${client.recommendation}"
)
public interface RecommendationVectorClient {

    @GetMapping("/internal/recommendations/product-vectors/{productId}")
    ResponseDto<ProductVectorInfo> getProductVector(@PathVariable UUID productId);
}
