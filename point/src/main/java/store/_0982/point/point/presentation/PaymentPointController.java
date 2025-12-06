package store._0982.point.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import store._0982.point.common.dto.PageResponse;
import store._0982.point.common.dto.ResponseDto;
import store._0982.point.point.application.PaymentPointService;
import store._0982.point.point.application.dto.*;
import store._0982.point.point.presentation.dto.PointChargeConfirmRequest;
import store._0982.point.point.presentation.dto.PointChargeCreateRequest;
import store._0982.point.point.presentation.dto.PointChargeFailRequest;
import store._0982.point.point.presentation.dto.PointMinusRequest;

import java.util.UUID;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/points")
public class PaymentPointController {
    private static final String MEMBER_ID_HEADER = "X-Member-Id";

    private final PaymentPointService paymentPointService;

    @Operation(summary = "포인트 충전 생성", description = "포인트 충전 requested 생성.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public ResponseDto<PaymentPointCreateInfo> createPaymentPoint(
            @RequestBody PointChargeCreateRequest request,
            @RequestHeader(MEMBER_ID_HEADER) @NotNull UUID memberId
    ) {
        PaymentPointCreateInfo info = paymentPointService.createPaymentPoint(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.CREATED.value(), info, "결제 요청 생성");
    }

    // TODO: 결제 성공 / 실패 이후 리다이렉트할 링크 설정 필요
    @Operation(summary = "포인트 충전 완료", description = "포인트 결제 및 충전 성공.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/confirm")
    public ResponseDto<PointChargeConfirmInfo> confirmPayment(@RequestBody @Valid PointChargeConfirmRequest request) {
        PointChargeConfirmInfo info = paymentPointService.confirmPayment(request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED.value(), info, "결제 및 포인트 충전 성공");
    }

    @Operation(summary = "포인트 결제 실패", description = "포인트 결제 실패시 정보 작성.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/fail")
    public ResponseDto<PointChargeFailInfo> handlePaymentFailure(@RequestBody @Valid PointChargeFailRequest request) {
        PointChargeFailInfo info = paymentPointService.handlePaymentFailure(request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED.value(), info, "결제 실패 정보 저장 완료");
    }

    @Operation(summary = "포인트 충전 내역 조회", description = "선택한 멤버의 포인트 충전 내역을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/payment")
    public ResponseDto<PageResponse<PaymentPointHistoryInfo>> getPaymentHistory(
            @RequestHeader(MEMBER_ID_HEADER) @NotNull UUID memberId,
            Pageable pageable
    ) {
        PageResponse<PaymentPointHistoryInfo> page = paymentPointService.getPaymentHistory(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK.value(), page, "포인트 충전 내역 조회 성공");
    }

    @Operation(summary = "포인트 조회", description = "선택한 멤버의 포인트를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<MemberPointInfo> getPoints(@RequestHeader(MEMBER_ID_HEADER) @NotNull UUID memberId) {
        MemberPointInfo info = paymentPointService.getPoints(memberId);
        return new ResponseDto<>(HttpStatus.OK.value(), info, "포인트 조회 성공");
    }

    @Operation(summary = "포인트 차감", description = "선택한 멤버의 포인트를 차감한다.")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/deduct")
    public ResponseDto<MemberPointInfo> deductPoints(
            @RequestHeader(MEMBER_ID_HEADER) @NotNull UUID memberId,
            @RequestBody @Valid PointMinusRequest request
    ) {
        MemberPointInfo info = paymentPointService.deductPoints(memberId, request);
        return new ResponseDto<>(HttpStatus.OK.value(), info, "포인트 차감 완료");
    }
}
