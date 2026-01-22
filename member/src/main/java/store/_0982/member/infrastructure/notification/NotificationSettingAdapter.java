package store._0982.member.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.notification.NotificationSetting;
import store._0982.member.domain.notification.NotificationSettingRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationSettingAdapter implements NotificationSettingRepository {
    
    private final NotificationSettingJpaRepository notificationSettingJpaRepository;

    @Override
    public List<NotificationSetting> findAllByMemberId(UUID memberId) {
        return notificationSettingJpaRepository.findAllByMemberId(memberId);
    }

    @Override
    public void save(NotificationSetting notificationSetting) {
        notificationSettingJpaRepository.save(notificationSetting);
    }

    @Override
    public void saveAll(Collection<NotificationSetting> notificationSettings) {
        notificationSettingJpaRepository.saveAll(notificationSettings);
    }
}
