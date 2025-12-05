package store._0982.product.presentation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.product.application.PurchaseService;
import store._0982.product.application.dto.GroupPurchaseInfo;
import store._0982.product.common.dto.PageResponseDto;
import store._0982.product.common.dto.ResponseDto;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/purchases")
public class GroupPurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "공동구매 상세 조회", description = "공동구매를 상세 조회한다.")
    @GetMapping("/{groupPurchaseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<GroupPurchaseInfo> getGroupPurchaseById(
            @PathVariable UUID groupPurchaseId) {
        GroupPurchaseInfo info = purchaseService.getGroupPurchaseById(groupPurchaseId);
        return new ResponseDto<>(HttpStatus.CREATED, info, "공동구매가 상세 조회되었습니다.");
    }

    @Operation(summary = "공동구매 목록 조회", description = "공동구매 목록을 페이징하여 조회한다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponseDto<GroupPurchaseInfo>> getGroupPurchase(
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponseDto<GroupPurchaseInfo> pageResponse = purchaseService.getGroupPurchase(pageable);
        return new ResponseDto<>(HttpStatus.OK, pageResponse, "공동구매 목록 조회 성공");
    }

}
