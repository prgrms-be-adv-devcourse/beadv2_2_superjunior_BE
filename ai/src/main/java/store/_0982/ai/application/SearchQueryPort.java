package store._0982.ai.application;

import store._0982.ai.infrastructure.feign.search.dto.GroupPurchaseSearchInfo;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;

import java.util.UUID;

public interface SearchQueryPort {
    ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchases(
            String keyword,
            String status,
            UUID sellerId,
            UUID memberId,
            String category,
            int page,
            int size
    );

    ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchasesBySimilarity(
            float[] vector,
            int page,
            int size
    );
}
