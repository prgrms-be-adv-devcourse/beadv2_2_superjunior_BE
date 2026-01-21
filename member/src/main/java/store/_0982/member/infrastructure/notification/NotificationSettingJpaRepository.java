package store._0982.member.infrastructure.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.notification.NotificationSetting;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingJpaRepository extends JpaRepository<NotificationSetting, UUID> {

    Optional<NotificationSetting> findByMemberIdAndChannel(UUID memberId, NotificationChannel channel);
}
