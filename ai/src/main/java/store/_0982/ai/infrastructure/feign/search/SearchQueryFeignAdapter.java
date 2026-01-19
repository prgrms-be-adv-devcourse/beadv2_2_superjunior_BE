package store._0982.ai.infrastructure.feign.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.ai.application.SearchQueryPort;
import store._0982.ai.application.dto.RecommandationSearchRequest;
import store._0982.ai.application.dto.RecommandationSearchResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchQueryFeignAdapter implements SearchQueryPort {

    private final SearchFeignClient searchFeignClient;


    @Override
    public List<RecommandationSearchResponse> getRecommandationCandidates(RecommandationSearchRequest recommandationSearchRequest) {
        return searchFeignClient.getRecommandationCandidates(recommandationSearchRequest);
    }
}
