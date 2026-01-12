package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.point.application.PointService;
import store._0982.point.application.dto.PointInfo;
import store._0982.point.presentation.dto.PointChargeRequest;
import store._0982.point.presentation.dto.PointDeductRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @Operation(summary = "포인트 충전", description = "포인트를 수동적으로 충전한다.")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/charge")
    public ResponseDto<PointInfo> chargePoints(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody @Valid PointChargeRequest request
    ) {
        PointInfo pointInfo = pointService.chargePoints(request.toCommand(), memberId);
        return new ResponseDto<>(HttpStatus.CREATED, pointInfo, "포인트 충전 성공");
    }

    @Operation(summary = "포인트 조회", description = "선택한 멤버의 포인트를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<PointInfo> getPoints(@RequestHeader(HeaderName.ID) UUID memberId) {
        PointInfo info = pointService.getPoints(memberId);
        return new ResponseDto<>(HttpStatus.OK, info, "포인트 조회 성공");
    }

    @Operation(summary = "포인트 결제", description = "포인트를 이용해 상품을 결제한다.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping("/deduct")
    public ResponseDto<PointInfo> deductPoints(
            @Valid @RequestBody PointDeductRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        PointInfo info = pointService.deductPoints(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, info, "포인트 결제 완료");
    }
}
