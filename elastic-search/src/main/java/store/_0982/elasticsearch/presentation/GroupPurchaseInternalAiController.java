package store._0982.elasticsearch.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.GroupPurchaseSearchService;
import store._0982.elasticsearch.application.dto.GroupPurchaseSearchInfo;
import store._0982.elasticsearch.application.dto.GroupPurchaseSimilaritySearchInfo;
import store._0982.elasticsearch.presentation.dto.GroupPurchaseAdvisorSearchRequest;
import store._0982.elasticsearch.presentation.dto.GroupPurchaseInternalKeywordSearchRequest;
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
                List.of("OPEN"),
                request.category(),
                request.vector(),
                request.topK()
        );
    }

    @Operation(summary = "공동구매 유사도 검색(advisor)")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PostMapping("/advisor")
    public List<GroupPurchaseSimilaritySearchInfo> searchForAdvisor(
            @RequestBody GroupPurchaseAdvisorSearchRequest request
    ){
        return searchService.searchGroupPurchaseDocumentWithEmbedding(
                request.keyword(),
                request.statuses(),
                request.category(),
                request.vector(),
                request.topK()
        );
    }

    @PostMapping("/keyword")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    public List<GroupPurchaseSearchInfo> searchGroupPurchaseByKeyword(
            @RequestBody GroupPurchaseInternalKeywordSearchRequest request
    ){
        int size = request.size() > 0 ? request.size() : 50;
        return searchService.searchGroupPurchaseDocument(
                request.keyword(),
                request.statuses(),
                null,
                request.category(),
                PageRequest.of(0,size)
        ).content();
    }
}
