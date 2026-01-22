package store._0982.ai.infrastructure.feign.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.ai.application.SearchQueryPort;
import store._0982.ai.application.dto.VectorSearchRequest;
import store._0982.ai.application.dto.VectorSearchResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchQueryFeignAdapter implements SearchQueryPort {

    private final SearchFeignClient searchFeignClient;


    @Override
    public List<VectorSearchResponse> getRecommandationCandidates(VectorSearchRequest vectorSearchRequest) {
        return searchFeignClient.getRecommandationCandidates(vectorSearchRequest);
    }
}
