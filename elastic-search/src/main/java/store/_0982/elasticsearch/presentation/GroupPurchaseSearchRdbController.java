package store._0982.elasticsearch.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.elasticsearch.application.GroupPurchaseSearchRdbService;
import store._0982.elasticsearch.application.dto.GroupPurchaseSearchInfo;

@Tag(name = "Group Purchase Search (RDB)", description = "RDB keyword search")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/searches/purchase")
public class GroupPurchaseSearchRdbController {

    private final GroupPurchaseSearchRdbService groupPurchaseSearchRdbService;

    @Operation(summary = "RDB keyword search", description = "title/description ILIKE 검색")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/search-db")
    public ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchaseRdb(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(required = false) java.util.UUID sellerId,
            Pageable pageable
    ) {
        PageResponse<GroupPurchaseSearchInfo> result = groupPurchaseSearchRdbService.searchByKeyword(
                keyword,
                status,
                category,
                sellerId,
                pageable
        );
        return new ResponseDto<>(HttpStatus.OK, result, "RDB search completed.");
    }
}
