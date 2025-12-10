package store._0982.product.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalApi;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.product.application.ParticipateService;
import store._0982.product.application.dto.GroupPurchaseThumbnailInfo;
import store._0982.product.application.dto.ParticipateInfo;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.presentation.dto.ParticipateRequest;
import store._0982.product.application.GroupPurchaseService;
import store._0982.product.application.dto.GroupPurchaseDetailInfo;
import store._0982.product.application.dto.GroupPurchaseInfo;

import store._0982.product.common.dto.PageResponseDto;
import store._0982.product.common.dto.ResponseDto;
import store._0982.product.presentation.dto.GroupPurchaseRegisterRequest;
import store._0982.product.presentation.dto.GroupPurchaseUpdateRequest;

import java.util.UUID;

@Tag(name="GroupPurchase", description = "")
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class GroupPurchaseController {
    private final GroupPurchaseService purchaseService;
    private final ParticipateService participateService;

    @Operation(summary="공동 구매 생성", description = "공동 구매를 생성합니다.")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<GroupPurchaseInfo> createGroupPurchase(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestHeader("X-Member-Role") String memberRole,
            @Valid @RequestBody GroupPurchaseRegisterRequest request
    ){
        GroupPurchaseInfo response = purchaseService.createGroupPurchase(memberId, memberRole, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, response, "공동 구매가 생성 되었습니다.");
    }

    @Operation(summary = "공동구매 상세 조회", description = "공동구매를 상세 조회한다.")
    @GetMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<GroupPurchaseDetailInfo> getGroupPurchaseById(
            @PathVariable UUID purchaseId) {
        GroupPurchaseDetailInfo info = purchaseService.getGroupPurchaseById(purchaseId);
        return new ResponseDto<>(HttpStatus.OK, info, "공동구매가 상세 조회되었습니다.");
    }

    @Operation(summary = "공동구매 목록 조회", description = "공동구매 목록을 페이징하여 조회한다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponseDto<GroupPurchaseThumbnailInfo>> getGroupPurchase(
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponseDto<GroupPurchaseThumbnailInfo> pageResponse = purchaseService.getGroupPurchase(pageable);
        return new ResponseDto<>(HttpStatus.OK, pageResponse, "공동구매 목록 조회되었습니다.");
    }

    @Operation(summary = "공동구매 판매자별 목록 조회", description = "공동구매 판매자별 목록을 페이징하여 조회한다.")
    @GetMapping("/seller/{sellerId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponseDto<GroupPurchaseThumbnailInfo>> getGroupPurchasesBySeller(
            @PathVariable UUID sellerId,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponseDto<GroupPurchaseThumbnailInfo> pageResponse = purchaseService.getGroupPurchasesBySeller(sellerId, pageable);
        return new ResponseDto<>(HttpStatus.OK, pageResponse, "공동구매 판매자별 목록 조회되었습니다");
    }

    @Operation(summary = "공동구매 삭제", description = "공동구매 삭제한다.")
    @DeleteMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<Void> deleteGroupPurchase(
            @PathVariable UUID purchaseId,
            @RequestHeader("X-Member-Id") UUID memberId) {
        purchaseService.deleteGroupPurchase(purchaseId, memberId);
        return new ResponseDto<>(HttpStatus.OK, null, "공동구매가 삭제되었습니다");
    }

    @InternalApi
    @Operation(summary = "공동구매 참여", description = "공동구매에 참여하여 참여자 수를 증가시킵니다. (Order 서비스 호출용)")
    @PostMapping("/{purchaseId}/participate")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<ParticipateInfo> participate(
            @PathVariable UUID purchaseId,
            @Valid @RequestBody ParticipateRequest request) {

        try {
            GroupPurchase groupPurchase = participateService.findGroupPurchaseById(purchaseId);
            ParticipateInfo result = participateService.participate(groupPurchase, request.quantity());

            if (result.success()) {
                return new ResponseDto<>(HttpStatus.OK, result, result.message());
            } else {
                return new ResponseDto<>(HttpStatus.OK, result, result.message());
            }
        } catch (CustomException e) {
            ParticipateInfo errorResult = ParticipateInfo.failure(e.getErrorCode().name(), 0, e.getMessage());
            return new ResponseDto<>(HttpStatus.OK, errorResult, e.getMessage());
        }
    }

    @Operation(summary = "공동구매 수정", description = "공동구매 정보를 수정한다.")
    @PatchMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<GroupPurchaseInfo> updateGroupPurchase(
            @PathVariable UUID purchaseId,
            @RequestHeader("X-Member-Role") String memberRole,
            @RequestHeader("X-Member-Id") UUID memberId,
            @Valid @RequestBody GroupPurchaseUpdateRequest request) {
        GroupPurchaseInfo response = purchaseService.updateGroupPurchase(memberId,  memberRole, purchaseId, request.toCommand());
        return new ResponseDto<>(HttpStatus.OK, response, "공동구매 정보가 수정되었습니다.");
    }

}
