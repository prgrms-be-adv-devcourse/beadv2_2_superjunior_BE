package store._0982.point.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.point.common.dto.PageResponse;
import store._0982.point.common.dto.ResponseDto;
import store._0982.point.point.application.PaymentPointService;
import store._0982.point.point.application.dto.MemberPointInfo;
import store._0982.point.point.application.dto.PaymentPointHistoryInfo;
import store._0982.point.point.application.dto.PointChargeConfirmInfo;
import store._0982.point.point.application.dto.PointChargeFailInfo;
import store._0982.point.point.presentation.dto.PointChargeConfirmRequest;
import store._0982.point.point.presentation.dto.PointChargeFailRequest;
import store._0982.point.point.presentation.dto.PointMinusRequest;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/points")
public class PaymentPointController {

    private final PaymentPointService paymentPointService;

    @Operation(summary = "포인트 충전 완료", description = "포인트 결제 및 충전 성공.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/confirm")
    public ResponseDto<PointChargeConfirmInfo> confirmPayment(@RequestBody PointChargeConfirmRequest request) {
        PointChargeConfirmInfo info = paymentPointService.confirmPayment(request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED.value(), info, "결제 및 포인트 충전 성공");
    }

    @Operation(summary = "포인트 결제 실패", description = "포인트 결제 실패시 정보 작성.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/fail")
    public ResponseDto<PointChargeFailInfo> handlePaymentFailure(@RequestBody PointChargeFailRequest request) {
        PointChargeFailInfo info = paymentPointService.handlePaymentFailure(request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED.value(), info, "결제 실패 정보 저장 완료");
    }

    @Operation(summary = "포인트 충전 내역 조회", description = "선택한 멤버의 포인트 충전 내역을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/payment")
    public ResponseDto<PageResponse<PaymentPointHistoryInfo>> getPaymentHistory(@RequestHeader("X-Member-Id") UUID memberId, Pageable pageable) {
        PageResponse<PaymentPointHistoryInfo> page = paymentPointService.getPaymentHistory(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK.value(), page, "포인트 충전 내역 조회 성공");
    }

    @Operation(summary = "포인트 조회", description = "선택한 멤버의 포인트를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<MemberPointInfo> getPoints(@RequestHeader("X-Member-Id") UUID memberId) {
        MemberPointInfo info = paymentPointService.getPoints(memberId);
        return new ResponseDto<>(HttpStatus.OK.value(), info, "포인트 조회 성공");
    }

    @Operation(summary = "포인트 차감", description = "선택한 멤버의 포인트를 차감한다.")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/minus")
    public ResponseDto<MemberPointInfo> deductPoints(@RequestHeader("X-Member-Id") UUID memberId, @RequestBody PointMinusRequest request) {
        MemberPointInfo info = paymentPointService.deductPoints(memberId, request);
        return new ResponseDto<>(HttpStatus.OK.value(), info, "포인트 차감 완료");
    }
}
