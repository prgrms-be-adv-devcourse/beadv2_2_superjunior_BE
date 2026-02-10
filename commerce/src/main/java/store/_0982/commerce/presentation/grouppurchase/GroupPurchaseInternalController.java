package store._0982.commerce.presentation.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.grouppurchase.GroupPurchasePerformanceService;
import store._0982.commerce.application.grouppurchase.GroupPurchaseSearchService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchasePerformanceInfo;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseSearchRow;
import store._0982.commerce.application.grouppurchase.dto.OpenGroupPurchaseInfo;
import store._0982.commerce.presentation.grouppurchase.dto.GroupPurchaseIdsRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/purchases")
public class GroupPurchaseInternalController {

    private final GroupPurchaseSearchService groupPurchaseSearchService;
    private final GroupPurchasePerformanceService groupPurchasePerformanceService;

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<GroupPurchaseSearchRow> findByIds(@RequestBody GroupPurchaseIdsRequest request) {
        return groupPurchaseSearchService.findSearchRowsByIds(request.ids());
    }

    @PostMapping("/performance")
    public List<GroupPurchasePerformanceInfo> getPerformance(
            @RequestBody List<UUID> groupPurchaseIds
    ){
        return groupPurchasePerformanceService.getPerformance(groupPurchaseIds);
    }

    @GetMapping("/open")
    @ResponseStatus(HttpStatus.OK)
    public List<OpenGroupPurchaseInfo> findOpenGroupPurchases(
            @RequestParam(value = "limit", defaultValue = "200") int limit
    ) {
        return groupPurchaseSearchService.findOpenGroupPurchases(limit, OffsetDateTime.now());
    }
}
