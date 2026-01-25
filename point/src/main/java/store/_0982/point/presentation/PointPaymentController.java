package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.point.application.dto.point.PointTransactionInfo;
import store._0982.point.application.point.*;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.presentation.dto.PointChargeRequest;
import store._0982.point.presentation.dto.PointDeductRequest;
import store._0982.point.presentation.dto.PointTransferRequest;
import store._0982.point.domain.constant.PointType;

import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointPaymentController {

    private final PointPaymentService pointPaymentService;
    private final PointChargeService pointChargeService;
    private final PointTransferService pointTransferService;
    private final PointDeductFacade pointDeductFacade;

    @Operation(summary = "포인트 충전", description = "포인트를 수동적으로 충전한다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/charge")
    public ResponseDto<PointBalanceInfo> chargePoints(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody @Valid PointChargeRequest request
    ) {
        PointBalanceInfo pointBalanceInfo = pointChargeService.chargePoints(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, pointBalanceInfo, "포인트 충전 성공");
    }

    @Operation(summary = "포인트 조회", description = "선택한 멤버의 포인트를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<PointBalanceInfo> getPoints(@RequestHeader(HeaderName.ID) UUID memberId) {
        PointBalanceInfo info = pointPaymentService.getPoints(memberId);
        return new ResponseDto<>(HttpStatus.OK, info, "포인트 조회 성공");
    }

    @Operation(summary = "포인트 결제", description = "포인트를 이용해 상품을 결제한다.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping("/deduct")
    public ResponseDto<PointBalanceInfo> deductPoints(
            @Valid @RequestBody PointDeductRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PointBalanceInfo info = pointDeductFacade.deductPoints(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, info, "포인트 결제 완료");
    }

    @Operation(summary = "포인트 충전 / 차감 이력 조회", description = "포인트 사용 이력을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/histories")
    public ResponseDto<Page<PointTransactionInfo>> getTransactions(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestParam PointType type,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PointTransactionInfo> response = pointPaymentService.getTransactions(memberId, type, pageable);
        return new ResponseDto<>(HttpStatus.OK, response, "포인트 이력 조회 성공");
    }

    @Operation(summary = "포인트 출금", description = "보유한 포인트를 출금한다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/transfer")
    public ResponseDto<PointBalanceInfo> transfer(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody @Valid PointTransferRequest request
    ) {
        PointBalanceInfo info = pointTransferService.transfer(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, info, "포인트 출금 성공");
    }
}
