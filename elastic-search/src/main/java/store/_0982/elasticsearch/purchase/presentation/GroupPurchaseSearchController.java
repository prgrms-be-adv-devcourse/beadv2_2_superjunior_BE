package store._0982.elasticsearch.purchase.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.elasticsearch.common.dto.ResponseDto;
import store._0982.elasticsearch.purchase.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.purchase.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.purchase.presentation.dto.GroupPurchaseIndexRequest;

//todo: pageresponse 적용, 각종 exception적용, command, info, 띄어쓰기 등 입력값 처리
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/searches")
public class GroupPurchaseSearchController {

    private final GroupPurchaseSearchService groupPurchaseSearchService;

    @Operation(summary = "공동구매 인덱스 생성", description = "공동구매의 인덱스가 없다면 생성, 있다면 매핑 정보 업데이트.")
    @ResponseStatus(HttpStatus.CREATED)
    @PutMapping("/index/purchase")
    public ResponseDto<Void> createGroupPurchaseIndex() {
        boolean created = groupPurchaseSearchService.createGroupPurchaseIndex();
        return created
                ? new ResponseDto<>(HttpStatus.CREATED, null, "인덱스 생성 완료")
                : new ResponseDto<>(HttpStatus.OK, null, "매핑 업데이트 완료");
    }

    @Operation(summary = "공동구매 인덱스 삭제", description = "공동구매의 인덱스를 삭제한다.")
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/index/purchase")
    public ResponseDto<Void> deleteGroupPurchaseIndex() {
        boolean deleted = groupPurchaseSearchService.deleteGroupPurchaseIndex();
        return deleted
                ? new ResponseDto<>(HttpStatus.OK, null, "인덱스 삭제 완료")
                : new ResponseDto<>(HttpStatus.NOT_FOUND, null, "삭제할 인덱스가 존재하지 않습니다.");

    }

    @Operation(summary = "공동구매 문서 추가", description = "공동구매 Elasticsearch 인덱스에 검색용 문서를 추가한다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/purchase")
    public ResponseDto<GroupPurchaseDocument> indexGroupPurchase(
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
            @org.springframework.web.bind.annotation.RequestBody GroupPurchaseIndexRequest request
    ) {
        GroupPurchaseDocument saved = groupPurchaseSearchService.index(request.toDocument());
        return new ResponseDto<>(HttpStatus.CREATED, saved, "공동구매 문서 생성 완료");
    }

    @Operation(summary = "공동구매 검색", description = "키워드 + 상태 + 정렬 기준으로 공동구매를 검색합니다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search")
    public ResponseDto<Page<GroupPurchaseDocument>> searchGroupPurchase(
            @RequestParam String keyword,
            @RequestParam String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        Page<GroupPurchaseDocument> result =
                groupPurchaseSearchService.search(keyword, status, page, size, sort);

        return new ResponseDto<>(HttpStatus.OK, result, "문서 검색 완료.");
    }
}
