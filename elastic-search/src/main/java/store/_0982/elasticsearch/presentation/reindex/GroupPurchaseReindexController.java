package store._0982.elasticsearch.presentation.reindex;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.exception.DefaultErrorCode;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseReindexInfo;
import store._0982.elasticsearch.application.dto.GroupPurchaseTotalReindexInfo;
import store._0982.elasticsearch.application.reindex.GroupPurchaseReindexService;

import java.time.OffsetDateTime;

@Tag(name = "Group Purchase Reindex", description = "공동구매 재색인")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/searches/purchase/reindex")
public class GroupPurchaseReindexController {

    private final GroupPurchaseReindexService reindexService;

    @Operation(summary = "새 인덱스 생성", description = "매핑/세팅 적용된 새 인덱스를 생성한다.")
    @ControllerLog
    @PostMapping("/index")
    public ResponseDto<GroupPurchaseReindexInfo> createIndex() {
        String indexName = reindexService.createIndex();
        return new ResponseDto<>(HttpStatus.CREATED, new GroupPurchaseReindexInfo(indexName, 0), "생성 완료");
    }

    @Operation(summary = "전체 재색인", description = "RDB 데이터를 새 인덱스로 전체 재색인한다.")
    @ControllerLog
    @PostMapping("/full")
    public ResponseDto<GroupPurchaseReindexInfo> reindexAll(
            @RequestParam(required = false) String targetIndex
    ) {
        GroupPurchaseReindexInfo info = reindexService.reindexAll(targetIndex);
        return new ResponseDto<>(HttpStatus.OK, info, "재색인 완료");
    }

    @Operation(summary = "증분 재색인", description = "updatedAt 기준 변경분만 재색인한다.")
    @ControllerLog
    @PostMapping("/incremental")
    public ResponseDto<GroupPurchaseReindexInfo> reindexIncremental(
            @RequestParam String targetIndex,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime since,
            @RequestParam(defaultValue = "false") boolean autoSwitch
    ) {
        if (targetIndex == null || targetIndex.isBlank()) {
            throw new CustomException(DefaultErrorCode.INVALID_PARAMETER);
        }
        GroupPurchaseReindexInfo reindexInfo = reindexService.reindexIncrementalAndMaybeSwitch(targetIndex, since, autoSwitch);
        return new ResponseDto<>(HttpStatus.OK, reindexInfo, "증분 재색인 완료");
    }

    @Operation(summary = "원클릭 재색인", description = "새 인덱스 생성 후 전체/증분 재색인을 순서대로 실행한다.")
    @ControllerLog
    @PostMapping("/total")
    public ResponseDto<GroupPurchaseTotalReindexInfo> reindexAllInOne(
            @RequestParam(defaultValue = "true") boolean autoSwitch
    ) {
        GroupPurchaseTotalReindexInfo summary = reindexService.reindexFullAndIncremental(autoSwitch);
        return new ResponseDto<>(HttpStatus.OK, summary, "재색인 사이클 완료");
    }
}
