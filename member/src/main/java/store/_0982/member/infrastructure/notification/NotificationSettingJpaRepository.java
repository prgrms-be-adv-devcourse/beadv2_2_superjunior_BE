package store._0982.member.infrastructure.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.notification.NotificationSetting;

import java.util.List;
import java.util.UUID;

public interface NotificationSettingJpaRepository extends JpaRepository<NotificationSetting, UUID> {

    List<NotificationSetting> findAllByMemberId(UUID memberId);
}
