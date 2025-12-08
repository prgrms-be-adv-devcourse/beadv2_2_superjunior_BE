package store._0982.product.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.product.application.PurchaseService;
import store._0982.product.application.dto.GroupPurchaseInfo;
import store._0982.product.application.dto.GroupPurchaseThumbnailInfo;
import store._0982.product.application.dto.PurchaseDetailInfo;
import store._0982.product.common.dto.PageResponseDto;
import store._0982.product.common.dto.ResponseDto;
import store._0982.product.presentation.dto.PurchaseRegisterRequest;
import store._0982.product.presentation.dto.PurchaseUpdateRequest;

import java.util.UUID;

@Tag(name="GroupPurchase", description = "")
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class GroupPurchaseController {
    private final PurchaseService purchaseService;

    @Operation(summary="공동 구매 생성", description = "공동 구매를 생성합니다.")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<PurchaseDetailInfo> createGroupPurchase(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestHeader("X-Member-Role") String memberRole,
            @Valid @RequestBody PurchaseRegisterRequest request
    ){
        PurchaseDetailInfo response = purchaseService.createGroupPurchase(memberId, memberRole, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, response, "공동 구매가 생성 되었습니다.");
    }

    @Operation(summary = "공동구매 상세 조회", description = "공동구매를 상세 조회한다.")
    @GetMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<GroupPurchaseInfo> getGroupPurchaseById(
            @PathVariable UUID purchaseId) {
        GroupPurchaseInfo info = purchaseService.getGroupPurchaseById(purchaseId);
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

    @Operation(summary = "공동구매 수정", description = "공동구매 정보를 수정한다.")
    @PatchMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PurchaseDetailInfo> updateGroupPurchase(
            @PathVariable UUID purchaseId,
            @RequestHeader("X-Member-Role") String memberRole,
            @RequestHeader("X-Member-Id") UUID memberId,
            @Valid @RequestBody PurchaseUpdateRequest request) {
        PurchaseDetailInfo response = purchaseService.updateGroupPurchase(memberId,  memberRole, purchaseId, request.toCommand());
        return new ResponseDto<>(HttpStatus.OK, response, "공동구매 정보가 수정되었습니다.");
    }

}
