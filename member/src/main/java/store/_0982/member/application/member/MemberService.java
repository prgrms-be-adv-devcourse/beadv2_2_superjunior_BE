package store._0982.member.application.member;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.member.application.member.dto.*;
import store._0982.member.application.member.event.MemberDeletedServiceEvent;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.domain.member.*;
import store._0982.member.exception.CustomErrorCode;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final AddressRepository addressRepository;
    private final EmailTokenRepository emailTokenRepository;

    private final EmailService emailService;
    private final NotificationSettingService notificationSettingService;

    private final ApplicationEventPublisher eventPublisher;
    private final PointQueryPort pointQueryPort;

    @ServiceLog
    @Transactional
    public MemberSignUpInfo createMember(MemberSignUpCommand command) {
        checkEmailDuplication(command.email());
        checkEmailVerification(command.email());
        checkNameDuplication(command.name());
        Member member = Member.create(command.email(), command.name(), command.password(), command.phoneNumber());
        member.encodePassword(passwordEncoder.encode(member.getSaltKey() + member.getPassword()));
        return MemberSignUpInfo.from(memberRepository.save(member));
    }

    @ServiceLog
    @Transactional
    public void createPointBalance(UUID memberId) throws FeignException {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        pointQueryPort.postPointBalance(memberId);
        member.confirm();
    }


    @ServiceLog
    @Transactional
    public void changePassword(PasswordChangeCommand command) {
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        checkPassword(command.password(), member);
        member.changePassword(passwordEncoder.encode(member.getSaltKey() + command.newPassword()));
    }

    @ServiceLog
    @Transactional
    public void deleteMember(MemberDeleteCommand command) {
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        checkPassword(command.password(), member);
        member.delete();
        eventPublisher.publishEvent(new MemberDeletedServiceEvent(command.memberId()));
        memberRepository.save(member);
    }

    @Transactional
    public ProfileUpdateInfo updateProfile(ProfileUpdateCommand command) {
        checkNameDuplication(command.name());
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        member.update(command.name(), command.phoneNumber());
        return ProfileUpdateInfo.from(member);
    }

    public ProfileInfo getProfile(UUID memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        return ProfileInfo.from(member);
    }

    private void checkPassword(String password, Member member) {
        if (!passwordEncoder.matches(member.getSaltKey() + password, member.getPassword())) {
            throw new CustomException(CustomErrorCode.WRONG_PASSWORD);
        }
    }

    private void checkEmailDuplication(String email) {
        if (memberRepository.findByEmail(email).isPresent())
            throw new CustomException(CustomErrorCode.DUPLICATED_EMAIL);
    }

    public void checkNameDuplication(String name) {
        if (memberRepository.findByName(name).isPresent()) throw new CustomException(CustomErrorCode.DUPLICATED_NAME);
    }

    public String getEmailAddress(UUID memberId) {
        return memberRepository.findById(memberId)
                .map(Member::getEmail)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
    }

    @ServiceLog
    @Transactional
    public void sendVerificationEmail(String email) {
        checkEmailDuplication(email);
        EmailToken emailToken = emailTokenRepository.findByEmail(email).map(EmailToken::refresh).orElse(EmailToken.create(email));
        emailTokenRepository.save(emailToken);
        emailService.sendEmail("no-reply@0909.store", email, "0909 이메일 인증 요청 메일입니다.", "0909 이메일 인증 코드입니다.\n 인증 코드: " + emailToken.getToken());
    }

    @ServiceLog
    @Transactional
    public void verifyEmail(EmailVerificationCommand command) {
        EmailToken emailToken = emailTokenRepository.findByEmail(command.email()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND));
        if (!emailToken.getToken().equals(command.token())) throw new CustomException(CustomErrorCode.WRONG_CODE);
        if (emailToken.isExpired()) throw new CustomException(CustomErrorCode.TIME_OUT);
        emailToken.verify();
    }

    private void checkEmailVerification(String email) {
        EmailToken emailToken = emailTokenRepository.findByEmail(email).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND));
        if (!emailToken.isVerified())
            throw new CustomException(CustomErrorCode.NOT_VERIFIED_EMAIL);
    }

    //여기 아래로는 Address 관련 메소드
    @Transactional
    public AddressInfo addAddress(AddressAddCommand command) {
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        Address address = Address.create(member, command.address(), command.addressDetail(), command.postalCode(), command.receiverName(), command.phoneNumber());
        return AddressInfo.from(addressRepository.save(address));
    }

    public PageResponse<AddressInfo> getAddresses(Pageable pageable, UUID memberId) {
        Page<Address> addresses = addressRepository.findAllByMemberId(pageable, memberId);
        Page<AddressInfo> infoPage = addresses.map(AddressInfo::from);
        return PageResponse.from(infoPage);
    }

    @Transactional
    public void deleteAddress(AddressDeleteCommand command) {
        Address address = addressRepository.findById(command.addressId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_ADDRESS));
        checkAddressOwner(address, command.memberId());
        addressRepository.deleteById(command.addressId());
    }

    private void checkAddressOwner(Address address, UUID memberId) {
        if (!address.getMember().getMemberId().equals(memberId))
            throw new CustomException(CustomErrorCode.FORBIDDEN);
    }

    public RoleInfo getRoleOfMember(UUID memberId) {
        Member member = memberRepository.findById(memberId).orElse(Member.createGuest());
        return new RoleInfo(memberId, member.getRole());
    }

    public List<UUID> getMemberIds(int currentPage, int pageSize) {
        Pageable pageable = PageRequest.of(currentPage, pageSize);
        return memberRepository.findIds(pageable).getContent();
    }
    @Transactional(noRollbackFor = CustomException.class)
    public void cancelMemberCreation(UUID memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        notificationSettingService.deleteSettings(memberId);
        memberRepository.hardDelete(member);
        throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
    }
}
