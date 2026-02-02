package store._0982.member.application.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.member.application.member.dto.*;
import store._0982.member.application.member.event.MemberDeletedServiceEvent;
import store._0982.member.domain.member.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final String EMAIL = "test@example.com";
    private static final String NAME = "tester";
    private static final String PASSWORD = "password123!";
    private static final String PHONE = "010-1234-5678";

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

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private MemberRoleCache memberRoleCache;

    @Mock
    private PointQueryPort pointQueryPort;

    @InjectMocks
    private MemberService memberService;

    @Test
    @DisplayName("회원 가입 시 인증된 이메일과 중복되지 않은 이름이면 가입 성공")
    void createMember_success() {
        MemberSignUpCommand command = new MemberSignUpCommand(EMAIL, PASSWORD, NAME, PHONE);
        EmailToken verifiedToken = EmailToken.create(EMAIL);
        verifiedToken.verify();

        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(emailTokenRepository.findByEmail(EMAIL)).thenReturn(Optional.of(verifiedToken));
        when(memberRepository.findByName(NAME)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MemberSignUpInfo result = memberService.createMember(command);

        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.name()).isEqualTo(NAME);
        assertThat(result.phoneNumber()).isEqualTo(PHONE);
        assertThat(result.memberId()).isNotNull();
        verify(memberRepository).findByEmail(EMAIL);
        verify(emailTokenRepository).findByEmail(EMAIL);
        verify(memberRepository).findByName(NAME);
        verify(passwordEncoder).encode(anyString());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("비밀번호 변경 시 현재 비밀번호 일치하면 갱신")
    void changePassword_success() {
        Member member = Member.create(EMAIL, NAME, "oldPassword", PHONE);
        UUID memberId = member.getMemberId();
        PasswordChangeCommand command = new PasswordChangeCommand(memberId, "oldPassword", "newPassword");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        memberService.changePassword(command);

        assertThat(member.getPassword()).isEqualTo("encodedNewPassword");
        verify(memberRepository).findById(memberId);
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(passwordEncoder).encode(anyString());
    }

    @Test
    @DisplayName("회원 탈퇴 시 이벤트 발행 후 저장")
    void deleteMember_success() {
        Member member = Member.create(EMAIL, NAME, PASSWORD, PHONE);
        UUID memberId = member.getMemberId();
        MemberDeleteCommand command = new MemberDeleteCommand(memberId, PASSWORD);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        doNothing().when(applicationEventPublisher).publishEvent(any(MemberDeletedServiceEvent.class));

        memberService.deleteMember(command);

        assertThat(member.getDeletedAt()).isNotNull();
        verify(memberRepository).findById(memberId);
        verify(memberRepository).save(member);
        verify(applicationEventPublisher).publishEvent(any(MemberDeletedServiceEvent.class));
    }

    @Test
    @DisplayName("프로필 수정 시 이름 중복이 없으면 업데이트")
    void updateProfile_success() {
        Member member = Member.create(EMAIL, NAME, PASSWORD, PHONE);
        UUID memberId = member.getMemberId();
        ProfileUpdateCommand command = new ProfileUpdateCommand(memberId, "newName", "010-3333-4444");

        when(memberRepository.findByName("newName")).thenReturn(Optional.empty());
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        ProfileUpdateInfo result = memberService.updateProfile(command);

        assertThat(result.name()).isEqualTo("newName");
        assertThat(result.phoneNumber()).isEqualTo("010-3333-4444");
        assertThat(member.getUpdatedAt()).isNotNull();
        verify(memberRepository).findByName("newName");
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("프로필 조회 시 회원 정보 반환")
    void getProfile_success() {
        Member member = Member.create(EMAIL, NAME, PASSWORD, PHONE);
        UUID memberId = member.getMemberId();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        ProfileInfo result = memberService.getProfile(memberId);

        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.email()).isEqualTo(EMAIL);
        assertThat(result.name()).isEqualTo(NAME);
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("인증 메일 요청 시 새 토큰을 생성하여 메일 발송")
    void sendVerificationEmail_createsNewToken() {
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(emailTokenRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        ArgumentCaptor<EmailToken> tokenCaptor = ArgumentCaptor.forClass(EmailToken.class);

        memberService.sendVerificationEmail(EMAIL);

        verify(emailTokenRepository).save(tokenCaptor.capture());
        EmailToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getEmail()).isEqualTo(EMAIL);
        assertThat(savedToken.getToken()).matches("\\d{6}");
        verify(emailService).sendEmail(
                eq("no-reply@0909.store"),
                eq(EMAIL),
                eq("0909 이메일 인증 요청 메일입니다."),
                argThat((String body) -> body.contains(savedToken.getToken()))
        );
    }

    @Test
    @DisplayName("인증 메일 요청 시 기존 토큰이 있으면 갱신 후 발송")
    void sendVerificationEmail_refreshesExistingToken() {
        EmailToken existing = EmailToken.create(EMAIL);
        String oldToken = existing.getToken();

        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(emailTokenRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        ArgumentCaptor<EmailToken> tokenCaptor = ArgumentCaptor.forClass(EmailToken.class);

        memberService.sendVerificationEmail(EMAIL);

        verify(emailTokenRepository).save(tokenCaptor.capture());
        EmailToken refreshed = tokenCaptor.getValue();
        assertThat(refreshed).isSameAs(existing);
        assertThat(refreshed.getToken()).matches("\\d{6}");
        if (!oldToken.equals(refreshed.getToken())) {
            assertThat(refreshed.getToken()).isNotEqualTo(oldToken);
        }
        verify(emailService).sendEmail(anyString(), eq(EMAIL), anyString(), argThat((String body) -> body.contains(refreshed.getToken())));
    }

    @Test
    @DisplayName("이메일 인증 시 토큰이 만료되지 않았다면 인증 처리")
    void verifyEmail_success() {
        EmailToken emailToken = EmailToken.create(EMAIL);
        EmailVerificationCommand command = new EmailVerificationCommand(EMAIL, emailToken.getToken());

        when(emailTokenRepository.findByEmail(EMAIL)).thenReturn(Optional.of(emailToken));

        memberService.verifyEmail(command);

        assertThat(emailToken.isVerified()).isTrue();
        verify(emailTokenRepository).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("주소 추가 시 회원을 찾으면 주소 저장")
    void addAddress_success() {
        Member member = Member.create(EMAIL, NAME, PASSWORD, PHONE);
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

        AddressInfo result = memberService.addAddress(command);

        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.address()).isEqualTo("서울시 테스트구");
        assertThat(result.addressDetail()).isEqualTo("101동 202호");
        verify(memberRepository).findById(memberId);
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("주소 목록 조회 시 페이지 응답 반환")
    void getAddresses_success() {
        Member member = Member.create(EMAIL, NAME, PASSWORD, PHONE);
        UUID memberId = member.getMemberId();
        Address address = Address.create(member, "서울시 테스트구", "101동 202호", "12345", "홍길동", "010-5555-6666");
        Pageable pageable = PageRequest.of(0, 10);
        Page<Address> page = new PageImpl<>(List.of(address), pageable, 1);

        when(addressRepository.findAllByMemberId(pageable, memberId)).thenReturn(page);

        PageResponse<AddressInfo> result = memberService.getAddresses(pageable, memberId);

        assertThat(result).isNotNull();
        verify(addressRepository).findAllByMemberId(pageable, memberId);
    }

    @Test
    @DisplayName("주소 삭제 시 회원 소유일 경우 삭제")
    void deleteAddress_success() {
        Member member = Member.create(EMAIL, NAME, PASSWORD, PHONE);
        Address address = Address.create(member, "서울시 테스트구", "101동 202호", "12345", "홍길동", "010-5555-6666");
        AddressDeleteCommand command = new AddressDeleteCommand(member.getMemberId(), address.getAddressId());

        when(addressRepository.findById(address.getAddressId())).thenReturn(Optional.of(address));

        memberService.deleteAddress(command);

        verify(addressRepository).findById(address.getAddressId());
        verify(addressRepository).deleteById(address.getAddressId());
    }

    @Test
    @DisplayName("이메일 인증 요청 시 이미 가입된 이메일이면 예외")
    void sendVerificationEmail_duplicateEmailThrows() {
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(Member.create(EMAIL, NAME, PASSWORD, PHONE)));

        assertThatThrownBy(() -> memberService.sendVerificationEmail(EMAIL))
                .isInstanceOf(CustomException.class);
        verify(memberRepository).findByEmail(EMAIL);
        verify(emailTokenRepository, never()).findByEmail(anyString());
    }
}
