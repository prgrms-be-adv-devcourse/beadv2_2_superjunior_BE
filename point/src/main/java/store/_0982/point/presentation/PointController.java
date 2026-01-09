package store._0982.point.presentation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.point.application.PointService;
import store._0982.point.application.dto.PointInfo;

import java.util.UUID;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;

    @Operation(summary = "포인트 조회", description = "선택한 멤버의 포인트를 조회한다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<PointInfo> getPoints(@RequestHeader(HeaderName.ID) UUID memberId) {
        PointInfo info = pointService.getPoints(memberId);
        return new ResponseDto<>(HttpStatus.OK, info, "포인트 조회 성공");
    }
}
