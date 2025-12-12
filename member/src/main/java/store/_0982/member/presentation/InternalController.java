package store._0982.member.presentation;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.member.application.MemberService;
import store._0982.member.application.dto.ProfileInfo;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/internal/members")
@Hidden
public class InternalController {
    private final MemberService memberService;

    @Operation(summary = "프로필 조회", description = "회원의 기본 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    @ControllerLog
    public ResponseDto<ProfileInfo> getProfile(@RequestHeader(value = HeaderName.ID) UUID memberId) {
        return new ResponseDto<>(HttpStatus.OK, memberService.getProfile(memberId), "프로필 정보");
    }
}
