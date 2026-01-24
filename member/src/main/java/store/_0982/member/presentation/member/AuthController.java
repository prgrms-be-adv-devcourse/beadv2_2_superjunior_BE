package store._0982.member.presentation.member;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import store._0982.common.dto.ResponseDto;
import store._0982.member.application.member.AuthService;
import store._0982.member.application.member.dto.LoginTokens;
import store._0982.member.presentation.member.dto.MemberLoginRequest;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${cookie.same-site}")
    private String sameSite;
    @Value("${cookie.secure}")
    private boolean secure;

    private static final Duration DURATION_OF_ACCESS_TOKEN_COOKIE =  Duration.ofHours(1);
    private static final Duration DURATION_OF_REFRESH_TOKEN_COOKIE =  Duration.ofDays(30);


    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 accessToken/refreshToken 쿠키를 발급합니다.")
    @PostMapping("/login")
    //setDomain은 필요 없음, 브라우저 입장에서는 발급 주소와 사용 위치 동일
    //TODO: 소프트딜리트 확인, 로그인, refresh도
    public ResponseDto<Void> login(@RequestBody MemberLoginRequest memberLoginRequest,
                                   HttpServletResponse response) {
        LoginTokens tokens = authService.login(memberLoginRequest.toCommand());

        ResponseCookie accessTokenCookie = generateAccessTokenCookie(tokens.accessToken(), DURATION_OF_ACCESS_TOKEN_COOKIE);
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        ResponseCookie refreshTokenCookie = generateRefreshTokenCookie(tokens.refreshToken(), DURATION_OF_REFRESH_TOKEN_COOKIE);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

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

        ResponseCookie accessTokenCookie = generateAccessTokenCookie(newAccessToken, DURATION_OF_ACCESS_TOKEN_COOKIE);
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());

        return new ResponseDto<>(HttpStatus.OK, null, "토큰이 발급되었습니다.");
    }

    @Operation(summary = "로그아웃", description = "accessToken과 refreshToken의 쿠키를 모두 만료시킵니다.")
    @GetMapping("/logout")
    public ResponseDto<Void> logout(HttpServletResponse response) {
        ResponseCookie accessDelete = generateAccessTokenCookie("", Duration.ofNanos(0));
        response.addHeader(HttpHeaders.SET_COOKIE, accessDelete.toString());

        ResponseCookie refreshDelete = generateRefreshTokenCookie("", Duration.ofNanos(0));
        response.addHeader(HttpHeaders.SET_COOKIE, refreshDelete.toString());

        return new ResponseDto<>(HttpStatus.OK, null, "로그아웃이 완료되었습니다.");
    }

    private ResponseCookie generateAccessTokenCookie(String accessToken, Duration maxAge) {
        return ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .path("/")  //api와 auth 전부 다 가야함 (logout 때문)
                .maxAge(maxAge)
                .sameSite(sameSite)
                .secure(secure)                  // apigateway와 클라이언트 간 https 설정 후 사용
                .build();
    }


    private ResponseCookie generateRefreshTokenCookie(String refreshToken, Duration maxAge) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .path("/auth")
                .maxAge(maxAge)
                .sameSite(sameSite)
                .secure(secure)
                .build();
    }

}
