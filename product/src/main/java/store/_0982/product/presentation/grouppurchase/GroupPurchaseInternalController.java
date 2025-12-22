package store._0982.product.presentation.grouppurchase;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ControllerLog;
import store._0982.product.application.grouppurchase.GroupPurchaseService;
import store._0982.product.application.grouppurchase.ParticipateService;
import store._0982.product.application.grouppurchase.dto.GroupPurchaseInfo;
import store._0982.product.application.grouppurchase.dto.GroupPurchaseInternalInfo;
import store._0982.product.application.grouppurchase.dto.ParticipateInfo;
import store._0982.product.domain.grouppurchase.GroupPurchase;
import store._0982.product.presentation.grouppurchase.dto.ParticipateRequest;

import java.util.List;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/internal/purchases")
@RequiredArgsConstructor
public class GroupPurchaseInternalController {

    private final GroupPurchaseService purchaseService;
    private final ParticipateService participateService;

    @ControllerLog
    @Operation(summary = "공동구매 참여", description = "공동구매에 참여하여 참여자 수를 증가시킵니다. (Order 서비스 호출용)")
    @PostMapping("/{purchaseId}/participate")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<ParticipateInfo> participate(
            @PathVariable UUID purchaseId,
            @Valid @RequestBody ParticipateRequest request) {
        try {
            GroupPurchase groupPurchase = participateService.findGroupPurchaseById(purchaseId);
            ParticipateInfo result = participateService.participate(groupPurchase, request.quantity());

            return new ResponseDto<>(HttpStatus.OK, result, result.message());
        } catch (CustomException e) {
            ParticipateInfo errorResult = ParticipateInfo.failure(e.getErrorCode().getHttpStatus().name(), 0, e.getMessage());
            return new ResponseDto<>(HttpStatus.OK, errorResult, e.getMessage());
        }
    }

    @Operation(summary = "공동구매 리스트 조회", description = "공동 구매 리스트 조회")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<List<GroupPurchaseInfo>> getGroupPurchaseByIds(
            @RequestParam("ids") List<UUID> ids) {
        List<GroupPurchaseInfo> info = purchaseService.getGroupPurchaseByIds(ids);
        return new ResponseDto<>(HttpStatus.OK, info, "공동구매 리스트가 조회되었습니다.");
    }


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
