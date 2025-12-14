package store._0982.product.presentation;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.log.ControllerLog;
import store._0982.product.application.GroupPurchaseService;
import store._0982.product.application.ParticipateService;
import store._0982.product.application.dto.*;
import store._0982.product.common.dto.ResponseDto;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.presentation.dto.ParticipateRequest;

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
            ParticipateInfo errorResult = ParticipateInfo.failure(e.getErrorCode().name(), 0, e.getMessage());
            return new ResponseDto<>(HttpStatus.OK, errorResult, e.getMessage());
        }
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
