package store._0982.member.infrastructure.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.notification.NotificationSetting;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingJpaRepository extends JpaRepository<NotificationSetting, UUID> {

    List<NotificationSetting> findAllByMemberId(UUID memberId);

    Optional<NotificationSetting> findByMemberIdAndChannel(UUID memberId, NotificationChannel channel);

    void deleteAllByMemberId(UUID memberId);
}
