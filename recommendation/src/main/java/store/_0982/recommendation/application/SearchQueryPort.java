package store._0982.recommendation.application;

import store._0982.recommendation.application.dto.AdvisorVectorSearchRequest;
import store._0982.recommendation.application.dto.VectorSearchRequest;
import store._0982.recommendation.application.dto.VectorSearchResponse;

import java.util.List;

public interface SearchQueryPort {

    List<VectorSearchResponse> getRecommandationCandidates(VectorSearchRequest vectorSearchRequest);

    List<VectorSearchResponse> getAdvisorCandidates(AdvisorVectorSearchRequest vectorSearchRequest);
}
