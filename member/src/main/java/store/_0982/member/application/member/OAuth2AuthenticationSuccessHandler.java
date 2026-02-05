package store._0982.member.application.member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import store._0982.common.exception.CustomException;
import store._0982.member.domain.member.CustomOAuth2User;
import store._0982.member.domain.member.Member;
import store._0982.member.domain.member.MemberRepository;
import store._0982.member.exception.CustomErrorCode;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final MemberRepository memberRepository;

    @Value("${app.oauth2.frontend-redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        Member member = memberRepository.findById(principal.getMemberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST));

        authService.issueTokens(member, response);

        response.sendRedirect(frontendRedirectUrl);
    }
}
