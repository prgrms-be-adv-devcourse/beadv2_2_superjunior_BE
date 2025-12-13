package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.point.application.PaymentPointService;
import store._0982.point.application.RefundService;
import store._0982.point.application.dto.*;
import store._0982.point.presentation.dto.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentPointController {
    private final PaymentPointService paymentPointService;
    private final RefundService refundService;

    @Operation(summary = "포인트 충전 생성", description = "포인트 충전 requested 생성.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping("/create")
    public ResponseDto<PaymentPointCreateInfo> createPaymentPoint(
            @RequestBody @Valid PointChargeCreateRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PaymentPointCreateInfo info = paymentPointService.createPaymentPoint(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.CREATED, info, "결제 요청 생성");
    }

    // TODO: 결제 성공 / 실패 이후 리다이렉트할 링크 설정 필요 (RedirectView 형태로 반환)
    @Operation(summary = "포인트 충전 완료", description = "포인트 결제 및 충전 성공.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @GetMapping("/confirm")
    public ResponseDto<PointChargeConfirmInfo> confirmPayment(@ModelAttribute @Valid PointChargeConfirmRequest request) {
        PointChargeConfirmInfo info = paymentPointService.confirmPayment(request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, info, "결제 및 포인트 충전 성공");
    }

    @Operation(summary = "포인트 결제 실패", description = "포인트 결제 실패시 정보 작성.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @GetMapping("/fail")
    public ResponseDto<PointChargeFailInfo> handlePaymentFailure(@ModelAttribute @Valid PointChargeFailRequest request) {
        PointChargeFailInfo info = paymentPointService.handlePaymentFailure(request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, info, "결제 실패 정보 저장 완료");
    }

    @Operation(summary = "포인트 환불", description = "기존 포인트 결제를 환불.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PostMapping("/refund")
    public ResponseDto<PointRefundInfo> refundPaymentPoint(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody @Valid PointRefundRequest request
    ) {
        PointRefundInfo info = refundService.refundPaymentPoint(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.OK, info, "포인트 결제 환불 완료");
    }

    @Operation(summary = "포인트 충전 내역 조회", description = "선택한 멤버의 포인트 충전 내역을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<PageResponse<PaymentPointHistoryInfo>> getPaymentHistory(
            @RequestHeader(HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<PaymentPointHistoryInfo> page = paymentPointService.getPaymentHistory(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, page, "포인트 충전 내역 조회 성공");
    }
}
