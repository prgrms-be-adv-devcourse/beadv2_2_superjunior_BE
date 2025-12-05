package store._0982.point.point.presentation;


import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import store._0982.point.common.dto.PageResponse;
import store._0982.point.common.dto.ResponseDto;
import store._0982.point.point.application.PaymentPointService;
import store._0982.point.point.application.dto.*;
import store._0982.point.point.presentation.dto.PointChargeConfirmRequest;
import store._0982.point.point.presentation.dto.PointChargeCreateRequest;

import org.springframework.data.domain.Pageable;
import store._0982.point.point.presentation.dto.PointChargeFailRequest;
import store._0982.point.point.presentation.dto.PointMinusRequest;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/points")
public class PaymentPointController {

    private final PaymentPointService paymentPointService;

    @Operation(summary = "포인트 충전 생성", description = "포인트 충전 requested 생성.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public ResponseDto<PaymentPointCreateInfo> pointPaymentCreate(@RequestBody PointChargeCreateRequest request, @RequestHeader("X-Member-Id") UUID memberId){
        return paymentPointService.pointPaymentCreate(request.toCommand(), memberId);
    }

    @Operation(summary = "포인트 충전 완료", description = "포인트 결제 및 충전 성공.")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/confirm")
    public ResponseDto<PointChargeConfirmInfo> pointPaymentConfirm(@RequestBody PointChargeConfirmRequest request){
        return paymentPointService.pointPaymentConfirm(request.toCommand());
    }

    @Operation(summary = "포인트 결제 실패", description = "포인트 결제 실패시 정보 작성.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/fail")
    public ResponseDto<PointChargeFailInfo> pointPaymentFail(@RequestBody PointChargeFailRequest request){
        return paymentPointService.pointPaymentFail(request.toCommand());
    }

    @Operation(summary = "포인트 충전 내역 조회", description = "선택한 멤버의 포인트 충전 내역을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/payment")
    public ResponseDto<PageResponse<PaymentPointHistoryInfo>> paymentHistoryFind(@RequestHeader("X-Member-Id") UUID memberId, Pageable pageable){
        return paymentPointService.paymentHistoryFind(memberId, pageable);
    }

    @Operation(summary = "포인트 조회", description = "선택한 멤버의 포인트를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<MemberPointInfo> pointCheck(@RequestHeader("X-Member-Id") UUID memberId){
        return paymentPointService.pointCheck(memberId);
    }

    @Operation(summary = "포인트 차감", description = "선택한 멤버의 포인트를 차감한다.")
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/minus")
    public ResponseDto<MemberPointInfo> pointMinus(@RequestHeader("X-Member-Id") UUID memberId, @RequestBody PointMinusRequest request){
        return paymentPointService.pointMinus(memberId, request);
    }
}
