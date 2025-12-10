package store._0982.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.member.application.AuthService;
import store._0982.member.application.dto.LoginTokens;
import store._0982.member.common.dto.ResponseDto;
import store._0982.member.presentation.dto.MemberLoginRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 accessToken/refreshToken 쿠키를 발급합니다.")
    @PostMapping("/login")
    public ResponseDto<Void> login(@RequestBody MemberLoginRequest memberLoginRequest,
                                   HttpServletResponse response) {
        LoginTokens tokens = authService.login(memberLoginRequest.toCommand());


        Cookie accessTokenCookie = new Cookie("accessToken", tokens.accessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/api");
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", tokens.refreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/api/auth/refresh");
        response.addCookie(refreshTokenCookie);


        return new ResponseDto<>(HttpStatus.OK, null, "로그인을 성공하였습니다.");
    }

    @Operation(summary = "액세스 토큰 재발급", description = "refreshToken 쿠키를 이용해 새로운 accessToken 쿠키를 발급합니다.")
    @GetMapping("/refresh")
    public ResponseDto<Void> refresh(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        String refreshToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        String newAccessToken = authService.refreshAccessToken(refreshToken);

        Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/api");
        response.addCookie(accessTokenCookie);

        return new ResponseDto<>(HttpStatus.OK, null, "토큰이 발급되었습니다.");
    }
}
