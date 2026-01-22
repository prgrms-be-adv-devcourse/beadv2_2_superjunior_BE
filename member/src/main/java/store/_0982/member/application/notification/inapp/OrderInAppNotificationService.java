package store._0982.member.application.notification.inapp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.application.notification.NotificationContentCreator;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.application.notification.dto.OrderCompletedCommand;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderInAppNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingService notificationSettingService;

    @Transactional
    public void notifyOrderCompleted(OrderCompletedCommand command) {
        UUID memberId = command.memberId();
        if (!notificationSettingService.isNotificationEnabled(memberId, NotificationChannel.IN_APP)) {
            return;
        }
        NotificationContent content = NotificationContentCreator.orderCompleted(command);
        Notification notification = Notification.from(content, NotificationChannel.IN_APP, memberId);
        notificationRepository.save(notification);
    }
}
