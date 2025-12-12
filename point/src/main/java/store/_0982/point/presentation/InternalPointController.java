package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.point.application.MemberPointService;
import store._0982.point.application.dto.MemberPointInfo;
import store._0982.point.presentation.dto.PointDeductRequest;
import store._0982.point.presentation.dto.PointReturnRequest;

import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/internal/points")
@RequiredArgsConstructor
public class InternalPointController {
    private final MemberPointService memberPointService;

    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping("/deduct")
    public ResponseDto<MemberPointInfo> deductPoints(
            @Valid @RequestBody PointDeductRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        MemberPointInfo info = memberPointService.deductPoints(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, info, "포인트 차감 완료");
    }

    @ResponseStatus(HttpStatus.CREATED)
    @ControllerLog
    @PostMapping("/return")
    public ResponseDto<MemberPointInfo> returnPoints(
            @Valid @RequestBody PointReturnRequest request,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        MemberPointInfo info = memberPointService.returnPoints(memberId, request.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, info, "포인트 반환 완료");
    }
}
