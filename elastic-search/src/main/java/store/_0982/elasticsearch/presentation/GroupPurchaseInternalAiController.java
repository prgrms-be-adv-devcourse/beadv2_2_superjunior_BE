package store._0982.elasticsearch.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseSearchInfo;

import java.util.List;
import java.util.UUID;

@Tag(name = "Group Purchase to Ai", description = "공동구매 Ai feign API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/searches/purchase")
public class GroupPurchaseInternalAiController {
    private final GroupPurchaseSearchService searchService;

    @Operation(summary = "공동구매 문서 임베딩 검색", description = "키워드(제목, 설명) + 상태 + 카테고리 + sellerId + embeddingVector")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search")
    public ResponseDto<List<GroupPurchaseSearchInfo>> searchGroupPurchase(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "") float[] vector,
            @RequestParam(defaultValue = "5") int topK
    ) {
        List<GroupPurchaseSearchInfo> result = searchService.searchGroupPurchaseDocumentWithEmbedding(
                keyword,
                status,
                sellerId,
                category,
                vector,
                topK
        );

        return new ResponseDto<>(HttpStatus.OK, result, "문서 검색 완료.");
    }
}
