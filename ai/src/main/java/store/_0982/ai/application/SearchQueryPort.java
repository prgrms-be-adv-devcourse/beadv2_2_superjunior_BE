package store._0982.ai.application;

import store._0982.ai.application.dto.RecommandationSearchRequest;
import store._0982.ai.application.dto.RecommandationSearchResponse;

import java.util.List;

public interface SearchQueryPort {

    List<RecommandationSearchResponse> getRecommandationCandidates(RecommandationSearchRequest recommandationSearchRequest);
}
