package store._0982.member.application.notification.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.member.application.member.EmailService;
import store._0982.member.application.member.MemberService;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.application.notification.NotificationService;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailDispatchService {

    private static final String NO_REPLY_EMAIL = "no-reply@0909.store";

    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;
    private final MemberService memberService;
    private final EmailService emailService;

    public void notifyToEmail(Notifiable notifiable) {
        UUID memberId = notifiable.memberId();
        if (notificationSettingService.isEnabled(memberId, NotificationChannel.EMAIL)) {
            String email = memberService.getEmailAddress(memberId);
            NotificationContent content = notifiable.content();

            // 이메일 발송 때문에 트랜잭션을 분리해야 함
            emailService.sendEmail(
                    NO_REPLY_EMAIL,
                    email,
                    content.title(),
                    content.message()
            );

            notificationService.saveNotification(notifiable, NotificationChannel.EMAIL);
        }
    }
}
