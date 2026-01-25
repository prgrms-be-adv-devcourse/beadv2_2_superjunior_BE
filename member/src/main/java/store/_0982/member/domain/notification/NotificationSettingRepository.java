package store._0982.member.domain.notification;

import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingRepository {

    Optional<NotificationSetting> findByMemberIdAndChannel(UUID memberId, NotificationChannel channel);

    List<NotificationSetting> findAllByMemberId(UUID memberId);

    void save(NotificationSetting notificationSetting);

    void saveAll(Collection<NotificationSetting> notificationSettings);

    void deleteAllByMemberId(UUID memberId);
}
