package store._0982.member.application.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import store._0982.common.dto.PageResponse;
import store._0982.member.application.member.dto.*;
import store._0982.member.domain.member.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private EmailTokenRepository emailTokenRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입에 성공한다")
    void createMember_success() {
        // given
        String email = "test@example.com";
        String name = "tester";
        String password = "password123!";
        String phone = "010-1234-5678";
        MemberSignUpCommand command = new MemberSignUpCommand(email, password, name, phone);

        EmailToken emailToken = EmailToken.create(email);
        emailToken.verify();

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(emailTokenRepository.findByEmail(email)).thenReturn(Optional.of(emailToken));
        when(memberRepository.findByName(name)).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // when
        MemberSignUpInfo result = memberService.createMember(command);

        // then
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.name()).isEqualTo(name);
        assertThat(result.phoneNumber()).isEqualTo(phone);
        assertThat(result.memberId()).isNotNull();

        verify(memberRepository).findByEmail(email);
        verify(emailTokenRepository).findByEmail(email);
        verify(memberRepository).findByName(name);
        verify(memberRepository).save(any(Member.class));
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    @DisplayName("비밀번호 변경에 성공한다")
    void changePassword_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "oldPassword", "010-1111-2222");
        UUID memberId = member.getMemberId();

        PasswordChangeCommand command = new PasswordChangeCommand(memberId, "oldPassword", "newPassword");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        // when
        memberService.changePassword(command);

        // then
        assertThat(member.getPassword()).isEqualTo("encodedNewPassword");
        verify(memberRepository).findById(memberId);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    @DisplayName("회원 탈퇴에 성공한다")
    void deleteMember_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        UUID memberId = member.getMemberId();

        MemberDeleteCommand command = new MemberDeleteCommand(memberId, "password");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // when
        memberService.deleteMember(command);

        // then
        assertThat(member.getDeletedAt()).isNotNull();
        verify(memberRepository).findById(memberId);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("프로필 수정에 성공한다")
    void updateProfile_success() {
        // given
        Member member = Member.create("test@example.com", "oldName", "password", "010-1111-2222");
        UUID memberId = member.getMemberId();

        String newName = "newName";
        String newPhone = "010-3333-4444";
        ProfileUpdateCommand command = new ProfileUpdateCommand(memberId, newName, newPhone);

        when(memberRepository.findByName(newName)).thenReturn(Optional.empty());
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        ProfileUpdateInfo result = memberService.updateProfile(command);

        // then
        assertThat(member.getName()).isEqualTo(newName);
        assertThat(member.getPhoneNumber()).isEqualTo(newPhone);
        assertThat(result.name()).isEqualTo(newName);
        assertThat(result.phoneNumber()).isEqualTo(newPhone);
        verify(memberRepository).findByName(newName);
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("프로필 조회에 성공한다")
    void getProfile_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        UUID memberId = member.getMemberId();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        ProfileInfo result = memberService.getProfile(memberId);

        // then
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.email()).isEqualTo(member.getEmail());
        assertThat(result.name()).isEqualTo(member.getName());
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("인증 메일 전송에 성공한다")
    void sendVerificationEmail_success() {
        // given
        String email = "test@example.com";

        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(emailTokenRepository.findByEmail(email)).thenReturn(Optional.empty());

        ArgumentCaptor<EmailToken> emailTokenCaptor = ArgumentCaptor.forClass(EmailToken.class);

        // when
        memberService.sendVerificationEmail(email);

        // then
        verify(memberRepository).findByEmail(email);
        verify(emailTokenRepository).findByEmail(email);
        verify(emailTokenRepository).save(emailTokenCaptor.capture());
        EmailToken savedToken = emailTokenCaptor.getValue();
        assertThat(savedToken.getEmail()).isEqualTo(email);
        assertThat(savedToken.getToken()).isNotNull();

        verify(emailService).sendEmail(
                eq("no-reply@0909.store"),
                eq(email),
                eq("0909 이메일 인증 요청 메일입니다."),
                org.mockito.ArgumentMatchers.contains("0909 이메일 인증 코드입니다.")
        );
    }

    @Test
    @DisplayName("이메일 인증에 성공한다")
    void verifyEmail_success() {
        // given
        String email = "test@example.com";
        String token = "test-token";
        EmailToken emailToken = EmailToken.create(email);

        when(emailTokenRepository.findByToken(token)).thenReturn(Optional.of(emailToken));

        // when
        memberService.verifyEmail(token);

        // then
        assertThat(emailToken.isVerified()).isTrue();
        verify(emailTokenRepository).findByToken(token);
    }

    @Test
    @DisplayName("주소 추가에 성공한다")
    void addAddress_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        UUID memberId = member.getMemberId();

        AddressAddCommand command = new AddressAddCommand(
                memberId,
                "서울시 테스트구",
                "101동 202호",
                "12345",
                "홍길동",
                "010-5555-6666"
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AddressInfo result = memberService.addAddress(command);

        // then
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.address()).isEqualTo("서울시 테스트구");
        assertThat(result.addressDetail()).isEqualTo("101동 202호");
        assertThat(result.postalCode()).isEqualTo("12345");
        verify(memberRepository).findById(memberId);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("주소 목록 조회에 성공한다")
    void getAddresses_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        UUID memberId = member.getMemberId();
        Address address = Address.create(member, "서울시 테스트구", "101동 202호", "12345", "홍길동", "010-5555-6666");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Address> addressPage = new PageImpl<>(List.of(address), pageable, 1);

        when(addressRepository.findAllByMemberId(pageable, memberId)).thenReturn(addressPage);

        // when
        PageResponse<AddressInfo> result = memberService.getAddresses(pageable, memberId);

        // then
        assertThat(result).isNotNull();
        verify(addressRepository).findAllByMemberId(pageable, memberId);
    }

    @Test
    @DisplayName("주소 삭제에 성공한다")
    void deleteAddress_success() {
        // given
        Member member = Member.create("test@example.com", "tester", "password", "010-1111-2222");
        Address address = Address.create(member, "서울시 테스트구", "101동 202호", "12345", "홍길동", "010-5555-6666");

        UUID memberId = member.getMemberId();
        UUID addressId = address.getAddressId();

        AddressDeleteCommand command = new AddressDeleteCommand(memberId, addressId);

        when(addressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // when
        memberService.deleteAddress(command);

        // then
        verify(addressRepository).findById(addressId);
        verify(addressRepository).deleteById(addressId);
    }
}

