package store._0982.elasticsearch.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseSearchInfo;
import store._0982.elasticsearch.presentation.dto.GroupPurchaseSimilaritySearchRequest;
import store._0982.elasticsearch.application.dto.GroupPurchaseSimilaritySearchInfo;

import java.util.UUID;

@Tag(name = "Group Purchase Search", description = "공동구매 검색 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/searches/purchase")
public class GroupPurchaseSearchController {

    private final GroupPurchaseSearchService groupPurchaseSearchService;

    @Operation(summary = "공동구매 문서 검색", description = "키워드(제목, 설명) + 상태 + 카테고리 + sellerId 기준으로 공동구매를 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search")
    public ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchase(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID sellerId,
            @RequestHeader(required = false, value = HeaderName.ID) UUID memberId,
            @RequestParam(defaultValue = "") String category,
            Pageable pageable
    ) {
        UUID effectiveSellerId = sellerId != null ? sellerId : memberId;
        PageResponse<GroupPurchaseSearchInfo> result = groupPurchaseSearchService.searchGroupPurchaseDocument(
                keyword,
                status,
                effectiveSellerId,
                category,
                pageable
        );

        return new ResponseDto<>(HttpStatus.OK, result, "문서 검색 완료.");
    }

    @Operation(summary = "공동구매 벡터 유사도 검색", description = "벡터 유사도 기준으로 공동구매를 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PostMapping("/search/similarity")
    public ResponseDto<PageResponse<GroupPurchaseSimilaritySearchInfo>> searchGroupPurchaseBySimilarity(
            @RequestBody GroupPurchaseSimilaritySearchRequest request,
            Pageable pageable
    ) {
        PageResponse<GroupPurchaseSimilaritySearchInfo> result = groupPurchaseSearchService.searchGroupPurchaseByVector(
                request.vector(),
                pageable
        );
        return new ResponseDto<>(HttpStatus.OK, result, "문서 검색 완료.");
    }
}
