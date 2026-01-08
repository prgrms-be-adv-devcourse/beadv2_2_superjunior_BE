package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ControllerLog;
import store._0982.common.log.LogFormat;
import store._0982.point.application.PaymentFailureService;
import store._0982.point.application.PaymentService;
import store._0982.point.application.PaymentRefundService;
import store._0982.point.application.dto.PaymentCreateInfo;
import store._0982.point.application.dto.PaymentInfo;
import store._0982.point.application.dto.PointRefundInfo;
import store._0982.point.exception.PaymentClientException;
import store._0982.point.presentation.dto.PaymentConfirmRequest;
import store._0982.point.presentation.dto.PaymentCreateRequest;
import store._0982.point.presentation.dto.PaymentFailRequest;
import store._0982.point.presentation.dto.PointRefundRequest;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final String PAYMENT_SUCCESS_ENDPOINT = "/point/charge/success";
    private static final String PAYMENT_FAIL_ENDPOINT = "/point/charge/fail";

    private final PaymentService paymentService;
    private final PaymentFailureService paymentFailureService;
    private final PaymentRefundService paymentRefundService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Operation(summary = "포인트 충전 생성", description = "포인트 충전 requested 생성.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping("/create")
    public ResponseDto<PaymentCreateInfo> createPaymentPoint(
            @RequestBody @Valid PaymentCreateRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PaymentCreateInfo info = paymentService.createPaymentPoint(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.CREATED, info, "결제 요청 생성");
    }

    @ControllerLog
    @PostMapping("/confirm")
    public RedirectView confirmPayment(
            @RequestBody @Valid PaymentConfirmRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        return handleExceptionWhenConfirmOrFail(() ->
                paymentService.confirmPayment(request.toCommand(), memberId), true);
    }

    @ControllerLog
    @PostMapping("/fail")
    public RedirectView handlePaymentFailure(
            @RequestBody @Valid PaymentFailRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        return handleExceptionWhenConfirmOrFail(() ->
                paymentFailureService.handlePaymentFailure(request.toCommand(), memberId), false);
    }

    @Operation(summary = "포인트 환불", description = "기존 포인트 결제를 환불.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PostMapping("/refund")
    public ResponseDto<PointRefundInfo> refundPaymentPoint(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody @Valid PointRefundRequest request
    ) {
        PointRefundInfo info = paymentRefundService.refundPaymentPoint(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.OK, info, "포인트 결제 환불 완료");
    }

    @Operation(summary = "포인트 충전 내역 조회", description = "선택한 멤버의 포인트 충전 내역을 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<PageResponse<PaymentInfo>> getPaymentHistories(
            @RequestHeader(HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<PaymentInfo> page = paymentService.getPaymentHistories(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, page, "포인트 충전 내역 조회 성공");
    }

    @Operation(summary = "포인트 충전 내역 상세 조회", description = "포인트 충전 내역의 상세 정보를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseDto<PaymentInfo> getPaymentHistory(
            @PathVariable UUID id,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PaymentInfo response = paymentService.getPaymentHistory(id, memberId);
        return new ResponseDto<>(HttpStatus.OK, response, "포인트 충전 내역 상세 조회 성공");
    }

    private RedirectView handleExceptionWhenConfirmOrFail(Supplier<PaymentInfo> supplier, boolean isSuccess) {
        try {
            PaymentInfo info = supplier.get();
            String url = isSuccess ? createSuccessUrl(info.paymentPointId()) : createFailureUrl(info.failMessage());
            return new RedirectView(url);
        } catch (CustomException e) {
            log.error(LogFormat.errorOf(e.getErrorCode().getHttpStatus(), e.getMessage()), e);
            return new RedirectView(createFailureUrl(e.getMessage()));
        } catch (PaymentClientException e) {
            log.error(LogFormat.errorOf(e.getStatus(), e.getMessage()), e);
            return new RedirectView(createFailureUrl(e.getMessage()));
        } catch (Exception e) {
            log.error(LogFormat.errorOf(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()), e);
            return new RedirectView(createFailureUrl("서버에서 에러가 발생했습니다."));
        }
    }

    private String createSuccessUrl(UUID paymentId) {
        return String.format("%s%s?paymentId=%s", frontendUrl, PAYMENT_SUCCESS_ENDPOINT, paymentId);
    }

    private String createFailureUrl(String message) {
        return String.format("%s%s?message=%s", frontendUrl, PAYMENT_FAIL_ENDPOINT, message);
    }
}
