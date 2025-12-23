package store._0982.commerce.presentation.grouppurchase;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.commerce.application.grouppurchase.ParticipateService;
import store._0982.commerce.application.grouppurchase.dto.ParticipateInfo;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.presentation.grouppurchase.dto.ParticipateRequest;
import store._0982.common.HeaderName;
import store._0982.common.auth.RequireRole;
import store._0982.common.auth.Role;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ControllerLog;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;

import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseDetailInfo;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseInfo;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseThumbnailInfo;
import store._0982.commerce.presentation.grouppurchase.dto.GroupPurchaseRegisterRequest;
import store._0982.commerce.presentation.grouppurchase.dto.GroupPurchaseUpdateRequest;

import java.util.List;
import java.util.UUID;

@Tag(name="GroupPurchase", description = "")
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class GroupPurchaseController {

    private final GroupPurchaseService purchaseService;

    @Operation(summary="공동 구매 생성", description = "공동 구매를 생성합니다.")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole({Role.SELLER, Role.ADMIN})
    public ResponseDto<GroupPurchaseInfo> createGroupPurchase(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @Valid @RequestBody GroupPurchaseRegisterRequest request
    ){
        GroupPurchaseInfo response = purchaseService.createGroupPurchase(memberId, request.toCommand());
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
    public ResponseDto<PageResponse<GroupPurchaseThumbnailInfo>> getGroupPurchase(
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<GroupPurchaseThumbnailInfo> pageResponse = purchaseService.getGroupPurchase(pageable);
        return new ResponseDto<>(HttpStatus.OK, pageResponse, "공동구매 목록 조회되었습니다.");
    }

    @Operation(summary = "공동구매 판매자별 목록 조회", description = "공동구매 판매자별 목록을 페이징하여 조회한다.")
    @GetMapping("/seller/{sellerId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<PageResponse<GroupPurchaseThumbnailInfo>> getGroupPurchasesBySeller(
            @PathVariable UUID sellerId,
            @PageableDefault(size = 10) Pageable pageable) {
        PageResponse<GroupPurchaseThumbnailInfo> pageResponse = purchaseService.getGroupPurchasesBySeller(sellerId, pageable);
        return new ResponseDto<>(HttpStatus.OK, pageResponse, "공동구매 판매자별 목록 조회되었습니다");
    }

    @ControllerLog
    @Operation(summary = "공동구매 삭제", description = "공동구매 삭제한다.")
    @DeleteMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseDto<Void> deleteGroupPurchase(
            @PathVariable UUID purchaseId,
            @RequestHeader(HeaderName.ID) UUID memberId) {
        purchaseService.deleteGroupPurchase(purchaseId, memberId);
        return new ResponseDto<>(HttpStatus.OK, null, "공동구매가 삭제되었습니다");
    }

    @Operation(summary = "공동구매 수정", description = "공동구매 정보를 수정한다.")
    @PatchMapping("/{purchaseId}")
    @ResponseStatus(HttpStatus.OK)
    @RequireRole({Role.SELLER, Role.ADMIN})
    public ResponseDto<GroupPurchaseInfo> updateGroupPurchase(
            @PathVariable UUID purchaseId,
            @RequestHeader(HeaderName.ID) UUID memberId,
            @Valid @RequestBody GroupPurchaseUpdateRequest request) {
        GroupPurchaseInfo response = purchaseService.updateGroupPurchase(memberId, purchaseId, request.toCommand());
        return new ResponseDto<>(HttpStatus.OK, response, "공동구매 정보가 수정되었습니다.");
    }

    private final ParticipateService participateService;
    


}
