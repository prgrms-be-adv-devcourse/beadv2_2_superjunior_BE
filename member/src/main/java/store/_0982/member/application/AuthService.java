package store._0982.member.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import store._0982.member.application.dto.LoginTokens;
import store._0982.member.application.dto.MemberLoginCommand;
import store._0982.member.common.exception.CustomErrorCode;
import store._0982.member.common.exception.CustomException;
import store._0982.member.domain.Member;
import store._0982.member.domain.MemberRepository;
import store._0982.member.infrastructure.JwtProvider;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;


    public LoginTokens login(MemberLoginCommand memberLoginCommand) {
        Member member = memberRepository.findByEmail(memberLoginCommand.email())
                .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST));

        if (passwordEncoder.matches(memberLoginCommand.password(), member.getPassword())) {
            throw new CustomException(CustomErrorCode.BAD_REQUEST);
        }

        String accessToken = jwtProvider.generateAccessToken(member);
        String refreshToken = jwtProvider.generateRefreshToken(member);

        //todo //redis에 등록해야함.

        return new LoginTokens(accessToken, refreshToken);
    }

    public String refreshAccessToken(String refreshToken) {
        if (refreshToken == null) {
            throw new CustomException(CustomErrorCode.BAD_REQUEST);
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
}
