package store._0982.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.application.dto.*;
import store._0982.member.common.exception.CustomErrorCode;
import store._0982.member.common.exception.CustomException;
import store._0982.member.domain.Member;
import store._0982.member.domain.MemberRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Transactional
    public MemberSignUpInfo signUpMember(MemberSignUpCommand command) {
        Member member = Member.create(command.email(), command.name(), command.password(), command.phoneNumber());
        member.encodePassword(passwordEncoder.encode(member.getSaltKey() + member.getPassword()));
        return MemberSignUpInfo.from(memberRepository.save(member));
    }

    @Transactional
    public void changePassword(PasswordChangeCommand command) {
        if (command.memberId() == null){
            throw new CustomException(CustomErrorCode.UNAUTHORIZED);
        }
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(()->new CustomException(CustomErrorCode.BAD_REQUEST));
        checkPassword(command.password(), member);
        member.changePassword(passwordEncoder.encode(member.getSaltKey() + command.newPassword()));
    }
    @Transactional
    public void deleteMember(MemberDeleteCommand command) {
        if (command.memberId() == null){
            throw new CustomException(CustomErrorCode.UNAUTHORIZED);
        }
        UUID memberId =  command.memberId();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(()->new CustomException(CustomErrorCode.BAD_REQUEST));
        checkPassword(command.password(), member);
        member.delete();
        memberRepository.save(member);
    }
    @Transactional
    public ProfileUpdateInfo updateProfile(ProfileUpdateCommand command) {
        if (command.memberId() == null){
            throw new CustomException(CustomErrorCode.UNAUTHORIZED);
        }
        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST));
        member.update(command.name(), command.phoneNumber());
        return ProfileUpdateInfo.from(member);
    }

    public ProfileInfo getProfile(UUID memberId) {
        if (memberId == null)
            throw new CustomException(CustomErrorCode.UNAUTHORIZED);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.BAD_REQUEST));
        return ProfileInfo.from(member);
    }

    private void checkPassword(String password, Member member) {
        if(!passwordEncoder.matches(member.getSaltKey() + password, member.getPassword())) {
            throw new CustomException(CustomErrorCode.WRONG_PASSWORD);
        }
    }

}
