package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.point.PointPaymentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/points")
public class InternalPointController {

    private final PointPaymentService pointPaymentService;

    @Operation(summary = "포인트 잔액 초기화", description = "회원 가입 시 멤버의 포인트 잔액을 새로 생성합니다.")
    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping
    public ResponseDto<PointBalanceInfo> initializeBalance(@RequestHeader(HeaderName.ID) UUID memberId) {
        PointBalanceInfo balanceInfo = pointPaymentService.initializeBalance(memberId);
        return new ResponseDto<>(HttpStatus.CREATED, balanceInfo, "회원 보유 포인트 초기화 성공");
    }
}
