package store._0982.commerce.presentation.grouppurchase;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseInternalInfo;

import java.util.List;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/internal/purchases")
@RequiredArgsConstructor
public class GroupPurchaseInternalController {

    private final GroupPurchaseService purchaseService;

    @GetMapping("/unsettled")
    @ResponseStatus(HttpStatus.OK)
    public List<GroupPurchaseInternalInfo> getUnsettledGroupPurchases() {
        return purchaseService.getUnsettledGroupPurchases();
    }

    @PutMapping("/{groupPurchaseId}/settle")
    @ResponseStatus(HttpStatus.OK)
    public void markAsSettled(@PathVariable UUID groupPurchaseId) {
        purchaseService.markAsSettled(groupPurchaseId);
    }

}
