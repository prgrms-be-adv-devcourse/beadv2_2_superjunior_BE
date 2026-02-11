package store._0982.recommendation.infrastructure.feign.commerce;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.common.dto.PageResponse;
import store._0982.recommendation.infrastructure.feign.commerce.dto.ProductPageResponse;

@Component
@RequiredArgsConstructor
public class CommerceProductQueryAdapter {

    private final CommerceProductFeignClient commerceProductFeignClient;

    public PageResponse<ProductPageResponse> fetchPage(int page, int size) {
        return commerceProductFeignClient.fetchEmbeddingPage(page, size);
    }
}
