package store._0982.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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
    //TODO: .setSecure, setAge
    //setDomain은 필요 없음, 브라우저 입장에서는 발급받은 곳도 localhost:8000 쓰는 곳도 localhost:8000
    //TODO: 소프트딜리트 확인, 로그인, refresh도
    public ResponseDto<Void> login(@RequestBody MemberLoginRequest memberLoginRequest,
                                   HttpServletResponse response) {
        LoginTokens tokens = authService.login(memberLoginRequest.toCommand());

        Cookie accessTokenCookie = generateAccessTokenCookie(tokens.accessToken());
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = generateRefreshTokenCookie(tokens.refreshToken());
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

        Cookie accessTokenCookie = generateAccessTokenCookie(newAccessToken);
        response.addCookie(accessTokenCookie);

        return new ResponseDto<>(HttpStatus.OK, null, "토큰이 발급되었습니다.");
    }

    private Cookie generateAccessTokenCookie(String accessToken) {
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/api");
//        accessTokenCookie.setMaxAge();
//        accessTokenCookie.setSecure(true);    //apigateway와 클라이언트 간 https 설정 후 사용
        return accessTokenCookie;
    }

    private Cookie generateRefreshTokenCookie(String refreshToken) {
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/api/auth/refresh");
//        refreshTokenCookie.setMaxAge();
//        refreshTokenCookie.setSecure(true);    //apigateway와 클라이언트 간 https 설정 후 사용
        return refreshTokenCookie;
    }

}


