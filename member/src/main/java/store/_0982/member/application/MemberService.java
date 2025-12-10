package store._0982.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.member.application.dto.*;
import store._0982.member.common.exception.CustomErrorCode;
import store._0982.member.domain.Address;
import store._0982.member.domain.AddressRepository;
import store._0982.member.domain.Member;
import store._0982.member.domain.MemberRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final AddressRepository addressRepository;

    //TODO: 이메일 검증 SMTP
    @Transactional
    public MemberSignUpInfo createMember(MemberSignUpCommand command) {
        checkEmailDuplication(command.email());
        checkNameDuplication(command.name());
        //TODO: 메일 인증 여부 체크
        Member member = Member.create(command.email(), command.name(), command.password(), command.phoneNumber());
        member.encodePassword(passwordEncoder.encode(member.getSaltKey() + member.getPassword()));
        return MemberSignUpInfo.from(memberRepository.save(member));
    }

    @Transactional
    public void changePassword(PasswordChangeCommand command) {
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        checkPassword(command.password(), member);
        member.changePassword(passwordEncoder.encode(member.getSaltKey() + command.newPassword()));
    }

    @Transactional
    public void deleteMember(MemberDeleteCommand command) {
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        checkPassword(command.password(), member);
        member.delete();
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
    @Transactional
    //여기 아래로는 Address 관련 메소드
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
        if(!address.getMember().getMemberId().equals(memberId))
            throw new CustomException(CustomErrorCode.FORBIDDEN);
    }
}
