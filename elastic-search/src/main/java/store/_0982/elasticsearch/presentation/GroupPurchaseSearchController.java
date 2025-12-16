package store._0982.elasticsearch.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentInfo;

import java.util.UUID;

@Tag(name = "Group Purchase Search", description = "공동구매 검색 및 색인")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/searches/purchase")
public class GroupPurchaseSearchController {

    private final GroupPurchaseSearchService groupPurchaseSearchService;

    @Operation(summary = "공동구매 인덱스 생성", description = "공동구매 인덱스 생성.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PutMapping("/index")
    public ResponseDto<Void> createGroupPurchaseIndex() {
        groupPurchaseSearchService.createGroupPurchaseIndex();
        return new ResponseDto<>(HttpStatus.CREATED, null, "인덱스 생성 완료");
    }

    @Operation(summary = "공동구매 인덱스 삭제", description = "공동구매의 인덱스를 삭제한다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @DeleteMapping("/index")
    public ResponseDto<Void> deleteGroupPurchaseIndex() {
        groupPurchaseSearchService.deleteGroupPurchaseIndex();
        return new ResponseDto<>(HttpStatus.OK, null, "인덱스 삭제 완료");
    }

    @Operation(summary = "공동구매 문서 검색", description = "키워드(제목, 설명) + 상태 기준으로 공동구매를 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search")
    public ResponseDto<PageResponse<GroupPurchaseDocumentInfo>> searchGroupPurchaseDocument(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestHeader(required = false, value = HeaderName.ID) UUID memberId,
            @RequestParam(defaultValue = "") String category,
            Pageable pageable
    ) {
        PageResponse<GroupPurchaseDocumentInfo> result = groupPurchaseSearchService.searchGroupPurchaseDocument(keyword, status, memberId, category, pageable);

        return new ResponseDto<>(HttpStatus.OK, result, "문서 검색 완료.");
    }

    @Operation(summary = "공동구매 문서 검색", description = "키워드(제목, 설명) + 상태 기준으로 공동구매를 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search/all")
    public ResponseDto<PageResponse<GroupPurchaseDocumentInfo>> searchAllGroupPurchaseDocument(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") String category,
            Pageable pageable
    ) {
        PageResponse<GroupPurchaseDocumentInfo> result = groupPurchaseSearchService.searchAllGroupPurchaseDocument(keyword, status, category, pageable);

        return new ResponseDto<>(HttpStatus.OK, result, "문서 검색 완료.");
    }

    @Operation(summary = "공동구매 문서 검색", description = "키워드(제목, 설명) + 상태 기준으로 공동구매를 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search/seller")
    public ResponseDto<PageResponse<GroupPurchaseDocumentInfo>> searchGroupPurchaseDocumentBySeller(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(defaultValue = "") String category,
            Pageable pageable
    ) {
        PageResponse<GroupPurchaseDocumentInfo> result = groupPurchaseSearchService.searchGroupPurchaseDocument(keyword, status, sellerId, category, pageable);

        return new ResponseDto<>(HttpStatus.OK, result, "문서 검색 완료.");
    }
}
