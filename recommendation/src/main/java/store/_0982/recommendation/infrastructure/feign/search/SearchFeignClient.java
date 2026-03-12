package store._0982.recommendation.infrastructure.feign.search;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import store._0982.recommendation.application.dto.AdvisorVectorSearchRequest;
import store._0982.recommendation.application.dto.VectorSearchRequest;
import store._0982.recommendation.application.dto.VectorSearchResponse;
import store._0982.recommendation.infrastructure.feign.search.dto.GroupPurchaseKeywordSearchRequest;
import store._0982.recommendation.infrastructure.feign.search.dto.GroupPurchaseSearchInfo;

import java.util.List;

@FeignClient(
        name = "search-service",
        url = "${client.search}"
)
public interface SearchFeignClient {
    @PostMapping("/internal/searches/purchase/search")
    List<VectorSearchResponse> getRecommandationCandidates(
            @RequestBody VectorSearchRequest vectorSearchRequest
    );

    @PostMapping("/internal/searches/purchase/advisor")
    List<VectorSearchResponse> getAdvisorCandidates(
            @RequestBody AdvisorVectorSearchRequest vectorSearchRequest
    );

    @PostMapping("/internal/searches/purchase/keyword")
    List<GroupPurchaseSearchInfo> searchByKeyword(
            @RequestBody GroupPurchaseKeywordSearchRequest request
    );
}
