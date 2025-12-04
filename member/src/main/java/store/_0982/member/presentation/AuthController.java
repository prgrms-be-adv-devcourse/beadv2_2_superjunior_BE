package store._0982.member.presentation;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import store._0982.member.application.AuthService;
import store._0982.member.application.dto.LoginTokens;
import store._0982.member.common.dto.ResponseDto;
import store._0982.member.presentation.dto.MemberLoginRequest;

@RestController("tmp")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/login")
    public ResponseDto<?> login(@RequestBody MemberLoginRequest memberLoginRequest,
                                HttpServletResponse response) {
        LoginTokens tokens = authService.login(memberLoginRequest.toCommand());


        Cookie accessTokenCookie = new Cookie("accessToken", tokens.accessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", tokens.refreshToken());
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/tmp/auth/refresh"); //swagger에서 숨길 때 tmp도 떼야함.
        response.addCookie(refreshTokenCookie);


        return new ResponseDto<>(HttpStatus.OK, null, "로그인을 성공하였습니다.");
    }

    @GetMapping("/auth/refresh")
    public ResponseDto<?> refresh(HttpServletRequest request, HttpServletResponse response) {
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
        accessTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);

        return new ResponseDto<>(HttpStatus.OK, null, "토큰이 발급되었습니다.");
    }
}
