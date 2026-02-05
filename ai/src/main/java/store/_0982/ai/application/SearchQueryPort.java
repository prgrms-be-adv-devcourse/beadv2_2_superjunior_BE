package store._0982.ai.application;

import store._0982.ai.application.dto.VectorSearchRequest;
import store._0982.ai.application.dto.VectorSearchResponse;

import java.util.List;

public interface SearchQueryPort {

    List<VectorSearchResponse> getRecommandationCandidates(VectorSearchRequest vectorSearchRequest);
}
