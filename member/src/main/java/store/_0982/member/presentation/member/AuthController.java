package store._0982.member.presentation.member;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.member.application.member.AuthService;
import store._0982.member.presentation.member.dto.MemberLoginRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 accessToken/refreshToken 쿠키를 발급합니다.")
    @PostMapping("/login")
    //setDomain은 필요 없음, 브라우저 입장에서는 발급 주소와 사용 위치 동일
    //TODO: 소프트딜리트 확인, 로그인, refresh도
    public ResponseDto<Void> login(@RequestBody MemberLoginRequest memberLoginRequest,
                                   HttpServletResponse response) {
        authService.login(response, memberLoginRequest.toCommand());
        return new ResponseDto<>(HttpStatus.OK, null, "로그인을 성공하였습니다.");
    }

    @Operation(summary = "액세스 토큰 재발급", description = "refreshToken 쿠키를 이용해 새로운 accessToken 쿠키를 발급합니다.")
    @GetMapping("/refresh")
    public ResponseDto<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        authService.refreshAccessTokenCookie(request, response);
        return new ResponseDto<>(HttpStatus.OK, null, "토큰이 발급되었습니다.");
    }

    @Operation(summary = "로그아웃", description = "accessToken과 refreshToken의 쿠키를 모두 만료시킵니다.")
    @GetMapping("/logout")
    public ResponseDto<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return new ResponseDto<>(HttpStatus.OK, null, "로그아웃이 완료되었습니다.");
    }

}
