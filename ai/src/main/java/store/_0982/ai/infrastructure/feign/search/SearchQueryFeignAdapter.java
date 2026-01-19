package store._0982.ai.infrastructure.feign.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.ai.application.SearchQueryPort;
import store._0982.ai.infrastructure.feign.search.dto.GroupPurchaseSearchInfo;
import store._0982.ai.infrastructure.feign.search.dto.GroupPurchaseSimilaritySearchRequest;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchQueryFeignAdapter implements SearchQueryPort {

    private final SearchFeignClient searchFeignClient;

    @Override
    public ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchases(
            String keyword,
            String status,
            UUID sellerId,
            UUID memberId,
            String category,
            int page,
            int size
    ) {
        return searchFeignClient.searchGroupPurchases(
                keyword,
                status,
                sellerId,
                memberId,
                category,
                page,
                size
        );
    }

    @Override
    public ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchasesBySimilarity(
            float[] vector,
            int page,
            int size
    ) {
        return searchFeignClient.searchGroupPurchasesBySimilarity(
                new GroupPurchaseSimilaritySearchRequest(vector),
                page,
                size
        );
    }
}
