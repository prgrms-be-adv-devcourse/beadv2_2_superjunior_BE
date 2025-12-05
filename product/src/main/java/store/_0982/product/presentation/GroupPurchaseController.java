package store._0982.product.presentation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.product.application.PurchaseService;
import store._0982.product.application.dto.GroupPurchaseInfo;
import store._0982.product.common.dto.ResponseDto;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/purchases")
public class GroupPurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "공동구매 조회", description = "공동구매를 조회한다.")
    @GetMapping("/{groupPurchaseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<GroupPurchaseInfo> getGroupPurchase(
            @PathVariable UUID groupPurchaseId) {
        GroupPurchaseInfo info = purchaseService.getGroupPurchase(groupPurchaseId);
        return new ResponseDto<>(HttpStatus.CREATED, info, "공동구매가 등록되었습니다.");
    }

}
