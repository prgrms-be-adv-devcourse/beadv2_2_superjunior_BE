package store._0982.member.application.notification.inapp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.member.application.notification.dto.OrderCompletedCommand;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.NotificationSettingRepository;

@Service
@RequiredArgsConstructor
public class OrderInAppNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public void notifyOrderCompleted(OrderCompletedCommand command) {

    }
}
