package store._0982.member.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingService notificationSettingService;

    @Transactional
    public void notifyToInApp(Notifiable notifiable) {
        UUID memberId = notifiable.memberId();
        if (!notificationSettingService.isEnabled(memberId, NotificationChannel.IN_APP)) {
            return;
        }
        NotificationContent content = notifiable.content();
        Notification notification = Notification.from(content, NotificationChannel.IN_APP, memberId);
        notificationRepository.save(notification);
    }
}
