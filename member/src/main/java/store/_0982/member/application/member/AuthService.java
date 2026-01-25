package store._0982.member.application.member;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.member.application.member.dto.LoginTokens;
import store._0982.member.application.member.dto.MemberLoginCommand;
import store._0982.member.domain.member.Member;
import store._0982.member.domain.member.MemberRepository;
import store._0982.member.exception.CustomErrorCode;
import store._0982.member.infrastructure.member.JwtProvider;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${cookie.same-site}")
    private String sameSite;
    @Value("${cookie.secure}")
    private boolean secure;

    private Duration durationOfAccessTokenCookie;
    private Duration durationOfRefreshTokenCookie;
    @PostConstruct
    public void init() {
        durationOfAccessTokenCookie =  Duration.ofMillis(jwtProvider.accessTokenCookieValidityPeriod);
        durationOfRefreshTokenCookie =  Duration.ofMillis(jwtProvider.refreshTokenCookieValidityPeriod);
    }


    @Transactional
    @ServiceLog
    public LoginTokens login(HttpServletResponse response, MemberLoginCommand memberLoginCommand) {
        Member member = memberRepository.findByEmail(memberLoginCommand.email()).orElseThrow(() -> new CustomException(CustomErrorCode.FAILED_LOGIN));
        checkPassword(member, memberLoginCommand.password());

        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(member);

        ResponseCookie accessTokenCookie = generateAccessTokenCookie(accessToken, durationOfAccessTokenCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        ResponseCookie refreshTokenCookie = generateRefreshTokenCookie(refreshToken, durationOfRefreshTokenCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        //TODO: //redis에 등록해야함.

        return new LoginTokens(accessToken, refreshToken);
    }

    private void checkPassword(Member member, String password) {
        if (!passwordEncoder.matches(member.getSaltKey() + password, member.getPassword())) {
            throw new CustomException(CustomErrorCode.FAILED_LOGIN);
        }
    }

    @ServiceLog
    public void refreshAccessTokenCookie(HttpServletRequest request, HttpServletResponse response) {
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

        String newAccessToken = refreshAccessToken(refreshToken);

        ResponseCookie accessTokenCookie = generateAccessTokenCookie(newAccessToken, durationOfAccessTokenCookie);
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
    }

    private String refreshAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(CustomErrorCode.NO_REFRESH_TOKEN);
        }
        try {
            UUID memberId = jwtProvider.getMemberIdFromToken(refreshToken);
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST));
            return jwtProvider.generateAccessToken(member);
        } catch (RuntimeException e) {
            throw new CustomException(CustomErrorCode.BAD_REQUEST);
        }
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


    public void logout(HttpServletResponse response) {
        ResponseCookie accessDelete = generateAccessTokenCookie("", Duration.ofNanos(0));
        response.addHeader(HttpHeaders.SET_COOKIE, accessDelete.toString());

        ResponseCookie refreshDelete = generateRefreshTokenCookie("", Duration.ofNanos(0));
        response.addHeader(HttpHeaders.SET_COOKIE, refreshDelete.toString());
    }
}
