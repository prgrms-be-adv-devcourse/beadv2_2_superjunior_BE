package store._0982.member.application.member;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import store._0982.member.application.member.dto.LoginTokens;
import store._0982.member.application.member.dto.MemberLoginCommand;
import store._0982.member.domain.member.Member;
import store._0982.member.domain.member.MemberRepository;
import store._0982.member.infrastructure.member.JwtProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("로그인에 성공하면 액세스 토큰과 리프레시 토큰을 발급한다")
    void login_success() {
        // given
        String email = "test@example.com";
        String password = "password123!";
        MemberLoginCommand command = new MemberLoginCommand(email, password);

        Member member = Member.create(email, "tester", "encodedPassword", "010-1111-2222");

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtProvider.generateAccessToken(member)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(member)).thenReturn("refresh-token");

        // when
        LoginTokens result = authService.login(command);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        verify(memberRepository).findByEmail(email);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(jwtProvider).generateAccessToken(member);
        verify(jwtProvider).generateRefreshToken(member);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 재발급한다")
    void refreshAccessToken_success() {
        // given
        String refreshToken = "refresh-token";
        UUID memberId = UUID.randomUUID();
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");

        when(jwtProvider.getMemberIdFromToken(refreshToken)).thenReturn(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(jwtProvider.generateAccessToken(member)).thenReturn("new-access-token");

        // when
        String newAccessToken = authService.refreshAccessToken(refreshToken);

        // then
        assertThat(newAccessToken).isEqualTo("new-access-token");
        verify(jwtProvider).getMemberIdFromToken(refreshToken);
        verify(memberRepository).findById(memberId);
        verify(jwtProvider).generateAccessToken(member);
    }

    @Test
    @DisplayName("로그아웃 시 토큰 쿠키의 만료 시간을 0으로 설정한다")
    void logout_success() {
        // given
        Cookie accessTokenCookie = new Cookie("accessToken", "access");
        Cookie refreshTokenCookie = new Cookie("refreshToken", "refresh");
        Cookie otherCookie = new Cookie("sessionId", "session");

        Cookie[] cookies = new Cookie[]{accessTokenCookie, refreshTokenCookie, otherCookie};

        // when
        List<Cookie> result = authService.logout(cookies);

        // then
        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Cookie::getName)
                .containsExactlyInAnyOrder("accessToken", "refreshToken");
        assertThat(accessTokenCookie.getMaxAge()).isZero();
        assertThat(refreshTokenCookie.getMaxAge()).isZero();
    }
}

