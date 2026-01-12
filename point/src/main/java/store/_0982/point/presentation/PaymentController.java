package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.point.application.payment.PaymentConfirmService;
import store._0982.point.application.payment.PaymentFailService;
import store._0982.point.application.payment.PaymentService;
import store._0982.point.application.dto.PaymentCreateInfo;
import store._0982.point.application.dto.PaymentInfo;
import store._0982.point.presentation.dto.PaymentConfirmRequest;
import store._0982.point.presentation.dto.PaymentCreateRequest;
import store._0982.point.presentation.dto.PaymentFailRequest;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentConfirmService paymentConfirmService;
    private final PaymentFailService paymentFailService;

    @Operation(summary = "PG 결제 주문 생성", description = "PG 결제를 위해 주문을 생성합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping("/create")
    public ResponseDto<PaymentCreateInfo> createPayment(
            @RequestBody @Valid PaymentCreateRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PaymentCreateInfo info = paymentService.createPayment(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.CREATED, info, "주문이 생성되었습니다.");
    }

    @Hidden
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ControllerLog
    @PostMapping("/confirm")
    public ResponseDto<Void> confirmPayment(
            @RequestBody @Valid PaymentConfirmRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        paymentConfirmService.confirmPayment(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.ACCEPTED, null, "결제 승인 완료");
    }

    @Hidden
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ControllerLog
    @PostMapping("/fail")
    public ResponseDto<Void> handlePaymentFailure(
            @RequestBody @Valid PaymentFailRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        paymentFailService.handlePaymentFailure(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.ACCEPTED, null, "결제 실패 처리 완료");
    }

    @Operation(summary = "PG 결제 내역 조회", description = "선택한 멤버의 PG 결제 내역을 조회합니다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<PageResponse<PaymentInfo>> getPaymentHistories(
            @RequestHeader(HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<PaymentInfo> page = paymentService.getPaymentHistories(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, page, "PG 결제 내역 조회 성공");
    }

    @Operation(summary = "PG 결제 내역 상세 조회", description = "PG 결제 내역의 상세 정보를 조회합니다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseDto<PaymentInfo> getPaymentHistory(
            @PathVariable UUID id,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PaymentInfo response = paymentService.getPaymentHistory(id, memberId);
        return new ResponseDto<>(HttpStatus.OK, response, "PG 결제 내역 상세 조회 성공");
    }
}
