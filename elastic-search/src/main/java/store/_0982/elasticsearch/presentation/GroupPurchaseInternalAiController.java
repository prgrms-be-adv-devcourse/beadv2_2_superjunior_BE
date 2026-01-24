package store._0982.elasticsearch.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseSimilaritySearchInfo;
import store._0982.elasticsearch.presentation.dto.GroupPurchaseInternalSearchRequest;

import java.util.List;

@Tag(name = "search internal 컨트롤러", description = "공동구매 관련 내부 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/searches/purchase")
public class GroupPurchaseInternalAiController {
    private final GroupPurchaseSearchService searchService;

    @Operation(summary = "공동구매 유사도 검색")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PostMapping("/search")
    public List<GroupPurchaseSimilaritySearchInfo> searchGroupPurchase(
            @RequestBody GroupPurchaseInternalSearchRequest request
    ) {
        return searchService.searchGroupPurchaseDocumentWithEmbedding(
                request.keyword(),
                "",//open으로 수정 해야함
                request.category(),
                request.vector(),
                request.topK()
        );
    }
}
