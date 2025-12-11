package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.point.application.MemberPointService;
import store._0982.point.application.dto.MemberPointInfo;
import store._0982.point.presentation.dto.PointMinusRequest;
import store._0982.point.presentation.dto.PointReturnRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class MemberPointController {
    private final MemberPointService memberPointService;

    @Operation(summary = "포인트 조회", description = "선택한 멤버의 포인트를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<MemberPointInfo> getPoints(@RequestHeader(HeaderName.ID) UUID memberId) {
        MemberPointInfo info = memberPointService.getPoints(memberId);
        return new ResponseDto<>(HttpStatus.OK, info, "포인트 조회 성공");
    }

    @Operation(summary = "포인트 차감", description = "선택한 멤버의 포인트를 차감한다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PatchMapping("/deduct")
    public ResponseDto<MemberPointInfo> deductPoints(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody @Valid PointMinusRequest request
    ) {
        MemberPointInfo info = memberPointService.deductPoints(memberId, request);
        return new ResponseDto<>(HttpStatus.OK, info, "포인트 차감 완료");
    }

    @Operation(summary = "포인트 반환", description = "선택한 멤버의 포인트를 반환한다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PatchMapping("/return")
    public ResponseDto<MemberPointInfo> returnPoints(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody @Valid PointReturnRequest request
    ) {
        MemberPointInfo info = memberPointService.returnPoints(memberId, request);
        return new ResponseDto<>(HttpStatus.OK, info, "포인트 반환 완료");
    }
}
