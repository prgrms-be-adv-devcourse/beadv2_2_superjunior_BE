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
import store._0982.point.application.pg.PgConfirmService;
import store._0982.point.application.pg.PgFailService;
import store._0982.point.application.pg.PgPaymentService;
import store._0982.point.application.dto.pg.PgCreateInfo;
import store._0982.point.application.dto.pg.PgPaymentInfo;
import store._0982.point.presentation.dto.PgConfirmRequest;
import store._0982.point.presentation.dto.PgCreateRequest;
import store._0982.point.presentation.dto.PgFailRequest;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments")
public class PgPaymentController {

    private final PgPaymentService pgPaymentService;
    private final PgConfirmService pgConfirmService;
    private final PgFailService pgFailService;

    @Operation(summary = "PG 결제 주문 생성", description = "PG 결제를 위해 주문을 생성합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping("/create")
    public ResponseDto<PgCreateInfo> createPayment(
            @RequestBody @Valid PgCreateRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PgCreateInfo info = pgPaymentService.createPayment(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.CREATED, info, "주문이 생성되었습니다.");
    }

    @Hidden
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ControllerLog
    @PostMapping("/confirm")
    public ResponseDto<Void> confirmPayment(
            @RequestBody @Valid PgConfirmRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        pgConfirmService.confirmPayment(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.ACCEPTED, null, "결제 승인 완료");
    }

    @Hidden
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ControllerLog
    @PostMapping("/fail")
    public ResponseDto<Void> handlePaymentFailure(
            @RequestBody @Valid PgFailRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        pgFailService.handlePaymentFailure(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.ACCEPTED, null, "결제 실패 처리 완료");
    }

    @Operation(summary = "PG 결제 내역 조회", description = "선택한 멤버의 PG 결제 내역을 조회합니다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<PageResponse<PgPaymentInfo>> getPaymentHistories(
            @RequestHeader(HeaderName.ID) UUID memberId,
            Pageable pageable
    ) {
        PageResponse<PgPaymentInfo> page = pgPaymentService.getPaymentHistories(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, page, "PG 결제 내역 조회 성공");
    }

    @Operation(summary = "PG 결제 내역 상세 조회", description = "PG 결제 내역의 상세 정보를 조회합니다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseDto<PgPaymentInfo> getPaymentHistory(
            @PathVariable UUID id,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PgPaymentInfo response = pgPaymentService.getPaymentHistory(id, memberId);
        return new ResponseDto<>(HttpStatus.OK, response, "PG 결제 내역 상세 조회 성공");
    }
}
