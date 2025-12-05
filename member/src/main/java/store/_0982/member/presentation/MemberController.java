package store._0982.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.member.application.MemberService;
import store._0982.member.application.dto.*;
import store._0982.member.common.dto.ResponseDto;
import store._0982.member.presentation.dto.MemberDeleteRequest;
import store._0982.member.presentation.dto.MemberSignUpRequest;
import store._0982.member.presentation.dto.PasswordChangeRequest;
import store._0982.member.presentation.dto.ProfileUpdateRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController { //todo log 달기
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "신규 회원을 등록합니다.")
    @PostMapping
    @Valid
    public ResponseDto<MemberSignUpInfo> signUpMember(@RequestBody MemberSignUpRequest memberSignUpRequest) {
        MemberSignUpInfo memberSignUpInfo = memberService.signUpMember(memberSignUpRequest.toCommand());
        return new ResponseDto<>(HttpStatus.OK, memberSignUpInfo, "회원가입이 완료되었습니다.");
    }

    @Operation(summary = "회원 탈퇴", description = "비밀번호를 확인하고 회원을 탈퇴 처리합니다.")
    @DeleteMapping
    @Valid
    public ResponseDto<Void> deleteMember(@RequestHeader(value = "X-Member-Id", required = false) UUID memberId, @RequestBody MemberDeleteRequest request) {
        memberService.deleteMember(new MemberDeleteCommand(memberId, request.password()));
        return new ResponseDto<>(HttpStatus.OK, null, "회원탈퇴가 완료되었습니다.");
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 검증한 후 새 비밀번호로 변경합니다.")
    @PutMapping("/password")
    @Valid
    public ResponseDto<Void> changePassword(
            @RequestHeader(value = "X-Member-Id", required = false) UUID memberId,
            @RequestBody PasswordChangeRequest request
    ) {
        PasswordChangeCommand command = new PasswordChangeCommand(memberId, request.password(), request.newPassword());
        memberService.changePassword(command);
        return new ResponseDto<>(HttpStatus.OK, null, "비밀번호가 변경되었습니다.");
    }

    @Operation(summary = "프로필 조회", description = "회원의 기본 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    @Valid
    public ResponseDto<ProfileInfo> getProfile(
            @RequestHeader(value = "X-Member-Id", required = false) UUID memberId
    ) {
        return new ResponseDto<>(HttpStatus.OK, memberService.getProfile(memberId), "프로필 정보");
    }

    @Operation(summary = "프로필 수정", description = "회원의 이름 및 전화번호 정보를 수정합니다.")
    @PutMapping("/profile")
    @Valid
    public ResponseDto<ProfileUpdateInfo> updateProfile(
            @RequestHeader(value = "X-Member-Id", required = false) UUID memberId, @RequestBody ProfileUpdateRequest request) {
        ProfileUpdateCommand command = new ProfileUpdateCommand(memberId, request.name(), request.phoneNumber());
        return new ResponseDto<>(HttpStatus.OK, memberService.updateProfile(command), "프로필 정보가 변경되었습니다.");
    }


    @Operation(summary = "헤더 확인", description = "게이트웨이에서 전달된 회원 헤더 정보를 확인합니다.")
    @GetMapping("/header-check")
    public ResponseDto<Map> checkHeader(
            @RequestHeader(value = "X-Member-Id", required = false) String memberId,
            @RequestHeader(value = "X-Member-Email", required = false) String email,
            @RequestHeader(value = "X-Member-Role", required = false) String role
    ) {

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Member-Id", memberId);
        headers.put("X-Member-Email", email);
        headers.put("X-Member-Role", role);

        return new ResponseDto<>(HttpStatus.OK, headers, "정상 헤더 출력");
    }
}
