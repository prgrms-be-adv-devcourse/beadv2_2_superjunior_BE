package store._0982.elasticsearch.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentInfo;
import store._0982.elasticsearch.presentation.dto.GroupPurchaseDocumentRequest;

import java.util.UUID;

//todo: doc 관련 api 호출 방식을 kafka 이벤트 처리 방식으로 변경
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

    @Operation(summary = "공동구매 문서 추가", description = "공동구매 Elasticsearch 인덱스에 검색용 문서를 추가한다.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping
    public ResponseDto<GroupPurchaseDocumentInfo> saveGroupPurchaseDocument(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공동구매 색인 요청",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "groupPurchaseId": "6d8f6a1b-1c4e-4b87-9d78-b3e1c9a21a11",
                                              "productId": "c3b42ab9-8d20-4d5c-a0b2-3a91bcbcd111",
                                              "minQuantity": 10,
                                              "maxQuantity": 100,
                                              "title": "아이폰 15 공동구매",
                                              "description": "아이폰 15를 최저가로 공동구매합니다.",
                                              "discountedPrice": 990000,
                                              "status": "OPEN",
                                              "startAt": "2025-03-08T10:00:00Z",
                                              "endAt": "2025-03-15T23:59:59Z",
                                              "createdAt": "2025-03-08T09:00:00Z",
                                              "updatedAt": "2025-03-08T09:00:00Z",
                                              "participants": 25
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody GroupPurchaseDocumentRequest request
    ) {
        GroupPurchaseDocumentInfo saved = groupPurchaseSearchService.saveGroupPurchaseDocument(request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, saved, "공동구매 문서 생성 완료");
    }

    @Operation(summary = "공동구매 문서 검색", description = "키워드(제목, 설명) + 상태 기준으로 공동구매를 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search")
    public ResponseDto<PageResponse<GroupPurchaseDocumentInfo>> searchGroupPurchaseDocument(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "OPEN") String status,
            Pageable pageable
    ) {
        PageResponse<GroupPurchaseDocumentInfo> result = groupPurchaseSearchService.searchGroupPurchaseDocument(keyword, status, pageable);

        return new ResponseDto<>(HttpStatus.OK, result, "문서 검색 완료.");
    }

    @Operation(summary = "공동구매 문서 삭제", description = "id 기준으로 공동구매 문서를 삭제합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @DeleteMapping("{groupPurchaseId}")
    public ResponseDto<Void> deleteGroupPurchaseDocument(@PathVariable UUID groupPurchaseId) {
        groupPurchaseSearchService.deleteGroupPurchaseDocument(groupPurchaseId);
        return new ResponseDto<>(HttpStatus.OK, null, "공동구매 문서 삭제 완료.");
    }
}
