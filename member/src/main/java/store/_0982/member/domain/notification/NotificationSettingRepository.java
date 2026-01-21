package store._0982.member.domain.notification;

import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingRepository {

    Optional<NotificationSetting> findByMemberIdAndChannel(UUID memberId, NotificationChannel channel);

    void save(NotificationSetting notificationSetting);
}
