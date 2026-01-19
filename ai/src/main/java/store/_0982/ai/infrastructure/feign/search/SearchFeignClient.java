package store._0982.ai.infrastructure.feign.search;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.ai.application.dto.RecommandationSearchRequest;
import store._0982.ai.application.dto.RecommandationSearchResponse;

import java.util.List;

@FeignClient(
        name = "search-service",
        url = "${client.search}"
)
public interface SearchFeignClient {
    @GetMapping("/internal/searches/purchase/search")
    List<RecommandationSearchResponse> getRecommandationCandidates(
            @RequestBody RecommandationSearchRequest recommandationSearchRequest
    );
}
