package store._0982.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.application.dto.MemberSignUpCommand;
import store._0982.member.application.dto.MemberSignUpInfo;
import store._0982.member.domain.Member;
import store._0982.member.domain.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public MemberSignUpInfo signUpMember(MemberSignUpCommand command) {
        if(!isVaildEmail(command.email()))
            throw new RuntimeException();
        Member member = Member.create(command.email(), command.name(), command.password(), command.phoneNumber());
        member.changePassword(passwordEncoder.encode(member.getSaltKey() + member.getPassword()));
        return MemberSignUpInfo.from(memberRepository.save(member));
    }

    private boolean isVaildEmail(String email){ // 이메일 인증 여부 체크
        //redis로 읽기
        return true;
    }
}
