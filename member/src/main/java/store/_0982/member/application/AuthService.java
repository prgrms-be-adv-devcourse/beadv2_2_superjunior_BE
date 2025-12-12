package store._0982.member.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.member.application.dto.LoginTokens;
import store._0982.member.application.dto.MemberLoginCommand;
import store._0982.member.common.exception.CustomErrorCode;
import store._0982.member.domain.Member;
import store._0982.member.domain.MemberRepository;
import store._0982.member.infrastructure.JwtProvider;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @ServiceLog
    public LoginTokens login(MemberLoginCommand memberLoginCommand) {
        Member member = memberRepository.findByEmail(memberLoginCommand.email())
                .orElseThrow(() -> new CustomException(CustomErrorCode.FAILED_LOGIN));

        checkPassword(member, memberLoginCommand.password());

        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(member);

        //TODO: //redis에 등록해야함.

        return new LoginTokens(accessToken, refreshToken);
    }

    @ServiceLog
    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(CustomErrorCode.NO_REFRESH_TOKEN);
        }
        try{
            UUID memberId = jwtProvider.getMemberIdFromToken(refreshToken);
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST));
            return jwtProvider.generateAccessToken(member);
        }catch (RuntimeException e){
            throw new CustomException(CustomErrorCode.BAD_REQUEST);
        }
    }

    private void checkPassword(Member member, String password){
        if (!passwordEncoder.matches(member.getSaltKey() + password, member.getPassword())) {
            throw new CustomException(CustomErrorCode.FAILED_LOGIN);
        }
    }
}
