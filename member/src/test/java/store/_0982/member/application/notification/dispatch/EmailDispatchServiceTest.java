package store._0982.member.application.notification.dispatch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.member.application.member.EmailService;
import store._0982.member.application.member.MemberService;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.application.notification.NotificationService;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailDispatchServiceTest {

    private static final String NO_REPLY_EMAIL = "no-reply@0909.store";

    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationSettingService notificationSettingService;

    @Mock
    private MemberService memberService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailDispatchService emailDispatchService;

    @Test
    @DisplayName("이메일 알림: 알림 설정이 활성화된 경우 이메일을 발송하고 알림을 저장한다")
    void notifyToEmail_success_whenEnabled() {
        // given
        UUID memberId = UUID.randomUUID();
        String memberEmail = "member@example.com";
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 완료",
                "주문이 완료되었습니다",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(memberService.getEmailAddress(memberId))
                .thenReturn(memberEmail);

        // when
        emailDispatchService.notifyToEmail(notifiable);

        // then
        verify(notificationSettingService).isEnabled(memberId, NotificationChannel.EMAIL);
        verify(memberService).getEmailAddress(memberId);
        verify(emailService).sendEmail(
                NO_REPLY_EMAIL,
                memberEmail,
                content.title(),
                content.message()
        );
        verify(notificationService).saveNotification(notifiable, NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("이메일 알림: 알림 설정이 비활성화된 경우 이메일을 발송하지 않고 알림을 저장하지 않는다")
    void notifyToEmail_skip_whenDisabled() {
        // given
        UUID memberId = UUID.randomUUID();
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 완료",
                "주문이 완료되었습니다",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(false);

        // when
        emailDispatchService.notifyToEmail(notifiable);

        // then
        verify(notificationSettingService).isEnabled(memberId, NotificationChannel.EMAIL);
        verify(memberService, never()).getEmailAddress(any());
        verify(emailService, never()).sendEmail(any(), any(), any(), any());
        verify(notificationService, never()).saveNotification(any(), any());
    }

    @Test
    @DisplayName("이메일 알림: 이메일 발송 실패 시 예외가 전파된다")
    void notifyToEmail_fail_whenEmailSendingFails() {
        // given
        UUID memberId = UUID.randomUUID();
        String memberEmail = "member@example.com";
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 완료",
                "주문이 완료되었습니다",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(memberService.getEmailAddress(memberId))
                .thenReturn(memberEmail);
        doThrow(new RuntimeException("이메일 발송 실패"))
                .when(emailService).sendEmail(any(), any(), any(), any());

        // when & then
        assertThatThrownBy(() -> emailDispatchService.notifyToEmail(notifiable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("이메일 발송 실패");

        verify(notificationService, never()).saveNotification(any(), any());
    }

    @Test
    @DisplayName("이메일 알림: 회원 이메일 조회 실패 시 예외가 전파된다")
    void notifyToEmail_fail_whenGetEmailFails() {
        // given
        UUID memberId = UUID.randomUUID();
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 완료",
                "주문이 완료되었습니다",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(memberService.getEmailAddress(memberId))
                .thenThrow(new RuntimeException("회원 정보 조회 실패"));

        // when & then
        assertThatThrownBy(() -> emailDispatchService.notifyToEmail(notifiable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("회원 정보 조회 실패");

        verify(emailService, never()).sendEmail(any(), any(), any(), any());
        verify(notificationService, never()).saveNotification(any(), any());
    }

    @Test
    @DisplayName("이메일 알림: 빈 이메일 주소에도 발송을 시도한다")
    void notifyToEmail_attempt_withEmptyEmail() {
        // given
        UUID memberId = UUID.randomUUID();
        String emptyEmail = "";
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 완료",
                "주문이 완료되었습니다",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(memberService.getEmailAddress(memberId))
                .thenReturn(emptyEmail);

        // when
        emailDispatchService.notifyToEmail(notifiable);

        // then
        verify(emailService).sendEmail(
                NO_REPLY_EMAIL,
                emptyEmail,
                content.title(),
                content.message()
        );
        verify(notificationService).saveNotification(notifiable, NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("이메일 알림: null 제목과 메시지에도 발송을 시도한다")
    void notifyToEmail_attempt_withNullTitleAndMessage() {
        // given
        UUID memberId = UUID.randomUUID();
        String memberEmail = "member@example.com";
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                null,
                null,
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(memberService.getEmailAddress(memberId))
                .thenReturn(memberEmail);

        // when
        emailDispatchService.notifyToEmail(notifiable);

        // then
        verify(emailService).sendEmail(
                NO_REPLY_EMAIL,
                memberEmail,
                null,
                null
        );
        verify(notificationService).saveNotification(notifiable, NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("이메일 알림: 특수 문자가 포함된 제목과 메시지를 정상적으로 처리한다")
    void notifyToEmail_success_withSpecialCharacters() {
        // given
        UUID memberId = UUID.randomUUID();
        String memberEmail = "member@example.com";
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 생성 <테스트> & \"특수문자\"",
                "주문이 생성되었습니다\n줄바꿈\t탭\r캐리지리턴",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(memberService.getEmailAddress(memberId))
                .thenReturn(memberEmail);

        // when
        emailDispatchService.notifyToEmail(notifiable);

        // then
        verify(emailService).sendEmail(
                NO_REPLY_EMAIL,
                memberEmail,
                content.title(),
                content.message()
        );
        verify(notificationService).saveNotification(notifiable, NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("이메일 알림: 매우 긴 제목과 메시지를 정상적으로 처리한다")
    void notifyToEmail_success_withLongContent() {
        // given
        UUID memberId = UUID.randomUUID();
        String memberEmail = "member@example.com";
        String longTitle = "A".repeat(1000);
        String longMessage = "B".repeat(10000);
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                longTitle,
                longMessage,
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(memberService.getEmailAddress(memberId))
                .thenReturn(memberEmail);

        // when
        emailDispatchService.notifyToEmail(notifiable);

        // then
        verify(emailService).sendEmail(
                NO_REPLY_EMAIL,
                memberEmail,
                longTitle,
                longMessage
        );
        verify(notificationService).saveNotification(notifiable, NotificationChannel.EMAIL);
    }

    @Test
    @DisplayName("이메일 알림: 알림 저장 실패 시에도 예외가 전파된다")
    void notifyToEmail_fail_whenSaveNotificationFails() {
        // given
        UUID memberId = UUID.randomUUID();
        String memberEmail = "member@example.com";
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "주문 완료",
                "주문이 완료되었습니다",
                UUID.randomUUID()
        );
        Notifiable notifiable = Notifiable.of(memberId, content);

        when(notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL))
                .thenReturn(true);
        when(memberService.getEmailAddress(memberId))
                .thenReturn(memberEmail);
        doThrow(new RuntimeException("DB 저장 실패"))
                .when(notificationService).saveNotification(any(), any());

        // when & then
        assertThatThrownBy(() -> emailDispatchService.notifyToEmail(notifiable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB 저장 실패");

        verify(emailService).sendEmail(any(), any(), any(), any());
    }
}
