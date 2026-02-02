package store._0982.member.application.member;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import store._0982.common.exception.CustomException;
import store._0982.member.application.member.dto.LoginTokens;
import store._0982.member.application.member.dto.MemberLoginCommand;
import store._0982.member.domain.member.Member;
import store._0982.member.domain.member.MemberRepository;
import store._0982.member.exception.CustomErrorCode;
import store._0982.member.infrastructure.member.JwtProvider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "sameSite", "Lax");
        ReflectionTestUtils.setField(authService, "secure", false);
    }

    @Test
    @DisplayName("로그인에 성공하면 토큰과 쿠키를 설정한다")
    void login_success() {
        String email = "test@example.com";
        String rawPassword = "password123!";
        MemberLoginCommand command = new MemberLoginCommand(email, rawPassword);
        Member member = Member.create(email, "tester", "storedPassword", "010-1111-2222");

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(eq(member.getSaltKey() + rawPassword), eq(member.getPassword()))).thenReturn(true);
        when(jwtProvider.generateAccessToken(member)).thenReturn("access-token");
        when(jwtProvider.generateRefreshToken(member)).thenReturn("refresh-token");

        LoginTokens result = authService.login(response, command);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");

        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
        List<String> cookies = cookieCaptor.getAllValues();
        assertThat(cookies).anySatisfy(value -> assertThat(value).contains("accessToken=access-token"));
        assertThat(cookies).anySatisfy(value -> assertThat(value).contains("refreshToken=refresh-token"));
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인에 실패한다")
    void login_wrongPassword_throws() {
        String email = "user@example.com";
        String rawPassword = "wrong";
        Member member = Member.create(email, "user", "stored", "010-0000-0000");

        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(eq(member.getSaltKey() + rawPassword), eq(member.getPassword()))).thenReturn(false);

        assertThatThrownBy(() -> authService.login(response, new MemberLoginCommand(email, rawPassword)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(CustomErrorCode.FAILED_LOGIN);
    }

    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰을 재발급하고 쿠키를 설정한다")
    void refreshAccessToken_success() {
        String refreshToken = "refresh-token";
        UUID memberId = UUID.randomUUID();
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        Cookie[] cookies = {new Cookie("refreshToken", refreshToken)};

        when(request.getCookies()).thenReturn(cookies);
        when(jwtProvider.getMemberIdFromToken(refreshToken)).thenReturn(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(jwtProvider.generateAccessToken(member)).thenReturn("new-access-token");

        authService.refreshAccessTokenCookie(request, response);

        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
        assertThat(cookieCaptor.getValue()).contains("accessToken=new-access-token");
    }

    @Test
    @DisplayName("리프레시 토큰이 없으면 예외가 발생한다")
    void refreshAccessToken_noCookie_throws() {
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> authService.refreshAccessTokenCookie(request, response))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(CustomErrorCode.NO_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("로그아웃하면 토큰 쿠키를 만료시킨다")
    void logout_success() {
        authService.logout(response);

        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
        verify(response, times(2)).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());
        List<String> cookies = cookieCaptor.getAllValues();
        assertThat(cookies).allSatisfy(value -> assertThat(value).contains("Max-Age=0"));
        assertThat(cookies).anySatisfy(value -> assertThat(value).contains("accessToken="));
        assertThat(cookies).anySatisfy(value -> assertThat(value).contains("refreshToken="));
    }
}
