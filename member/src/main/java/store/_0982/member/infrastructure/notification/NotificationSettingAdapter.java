package store._0982.member.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.notification.NotificationSetting;
import store._0982.member.domain.notification.NotificationSettingRepository;
import store._0982.member.domain.notification.constant.NotificationChannel;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationSettingAdapter implements NotificationSettingRepository {
    
    private final NotificationSettingJpaRepository notificationSettingJpaRepository;

    @Override
    public Optional<NotificationSetting> findByMemberIdAndChannel(UUID memberId, NotificationChannel channel) {
        return notificationSettingJpaRepository.findByMemberIdAndChannel(memberId, channel);
    }

    @Override
    public void save(NotificationSetting notificationSetting) {
        notificationSettingJpaRepository.save(notificationSetting);
    }

    @Override
    public void saveAll(Iterable<NotificationSetting> notificationSettings) {
        notificationSettingJpaRepository.saveAll(notificationSettings);
    }
}
