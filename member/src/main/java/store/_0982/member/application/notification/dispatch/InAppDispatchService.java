package store._0982.member.application.notification.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.application.notification.NotificationService;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InAppDispatchService {

    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;

    @Transactional
    public void notifyToInApp(Notifiable notifiable) {
        UUID memberId = notifiable.memberId();
        if (notificationSettingService.isEnabled(memberId, NotificationChannel.IN_APP)) {
            notificationService.saveNotification(notifiable.content(), NotificationChannel.IN_APP, memberId);
        }
    }
}
