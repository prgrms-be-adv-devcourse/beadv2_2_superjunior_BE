package store._0982.member.domain.notification;

import java.util.List;
import java.util.UUID;

public interface NotificationSettingRepository {

    List<NotificationSetting> findAllByMemberId(UUID memberId);

    void save(NotificationSetting notificationSetting);

    void saveAll(Iterable<NotificationSetting> notificationSettings);
}
