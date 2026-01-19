package store._0982.ai.infrastructure.feign.search;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import store._0982.ai.infrastructure.feign.search.dto.GroupPurchaseSearchInfo;
import store._0982.ai.infrastructure.feign.search.dto.GroupPurchaseSimilaritySearchRequest;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;

import java.util.UUID;

@FeignClient(
        name = "search-service",
        url = "${client.search}"
)
public interface SearchFeignClient {

    @GetMapping("/api/searches/purchase/search")
    ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchases(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID sellerId,
            @RequestHeader(value = HeaderName.ID, required = false) UUID memberId,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @PostMapping("/api/searches/purchase/search/similarity")
    ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchasesBySimilarity(
            @RequestBody GroupPurchaseSimilaritySearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );
}
