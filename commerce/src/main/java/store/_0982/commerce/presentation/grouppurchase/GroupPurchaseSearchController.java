package store._0982.commerce.presentation.grouppurchase;

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
import store._0982.commerce.application.grouppurchase.GroupPurchaseSearchService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchInfo;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;

import java.util.UUID;

@Tag(name = "Group Purchase Search (DB)", description = "Group purchase DB search API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/purchases/search")
public class GroupPurchaseSearchController {

    private final GroupPurchaseSearchService groupPurchaseSearchService;

    @Operation(summary = "Group purchase DB search", description = "Search group purchases using DB LIKE filters.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @GetMapping("/db")
    public ResponseDto<PageResponse<GroupPurchaseSearchInfo>> searchGroupPurchase(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(defaultValue = "") String category,
            Pageable pageable
    ) {
        PageResponse<GroupPurchaseSearchInfo> result = groupPurchaseSearchService.searchGroupPurchasesByDb(
                keyword,
                status,
                sellerId,
                category,
                pageable
        );
        return new ResponseDto<>(HttpStatus.OK, result, "DB search completed.");
    }
}
