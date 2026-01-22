package store._0982.member.application.notification.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.application.notification.BulkNotifiable;
import store._0982.member.application.notification.Notifiable;
import store._0982.member.application.notification.NotificationService;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InAppDispatchService {

    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;

    @Transactional
    public void notifyToInApp(Notifiable notifiable) {
        if (notificationSettingService.isEnabled(notifiable.memberId(), NotificationChannel.IN_APP)) {
            notificationService.saveNotification(notifiable, NotificationChannel.IN_APP);
        }
    }

    @Transactional
    public void notifyToInApp(BulkNotifiable bulkNotifiable) {
        List<Notifiable> enabledNotifications = bulkNotifiable.notifiables().stream()
                .filter(notifiable -> notificationSettingService.isEnabled(notifiable.memberId(), NotificationChannel.IN_APP))
                .toList();

        if (!enabledNotifications.isEmpty()) {
            notificationService.saveBulkNotifications(enabledNotifications, NotificationChannel.IN_APP);
        }
    }
}
