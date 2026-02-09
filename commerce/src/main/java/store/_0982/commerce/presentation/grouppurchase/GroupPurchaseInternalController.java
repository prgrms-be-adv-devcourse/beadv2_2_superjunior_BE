package store._0982.commerce.presentation.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store._0982.commerce.application.grouppurchase.GroupPurchasePerformanceService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchasePerformanceInfo;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
public class GroupPurchaseInternalController {

    private final GroupPurchasePerformanceService groupPurchasePerformanceService;

    @PostMapping("/purchases/performance")
    public List<GroupPurchasePerformanceInfo> getPerformance(
            @RequestBody List<UUID> groupPurchaseIds
    ){
        return groupPurchasePerformanceService.getPerformance(groupPurchaseIds);
    }
}
