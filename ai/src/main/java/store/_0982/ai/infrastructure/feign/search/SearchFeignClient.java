package store._0982.ai.infrastructure.feign.search;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.ai.application.dto.VectorSearchRequest;
import store._0982.ai.application.dto.VectorSearchResponse;

import java.util.List;

@FeignClient(
        name = "search-service",
        url = "${client.search}"
)
public interface SearchFeignClient {
    @GetMapping("/internal/searches/purchase/search")
    List<VectorSearchResponse> getRecommandationCandidates(
            @RequestBody VectorSearchRequest vectorSearchRequest
    );
}
