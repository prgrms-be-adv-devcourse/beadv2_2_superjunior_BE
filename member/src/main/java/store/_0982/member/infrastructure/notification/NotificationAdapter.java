package store._0982.member.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class NotificationAdapter implements NotificationRepository {
    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Optional<Notification> findById(UUID uuid) {
        return notificationJpaRepository.findById(uuid);
    }

    @Override
    public List<Notification> findByMemberIdAndStatus(UUID memberId, NotificationStatus status) {
        return notificationJpaRepository.findByMemberIdAndStatus(memberId, status);
    }

    public Page<Notification> findByMemberIdAndChannel(UUID memberId, NotificationChannel channel, Pageable pageable) {
        return notificationJpaRepository.findByMemberIdAndChannel(memberId, channel, pageable);
    }

    @Override
    public Page<Notification> findByMemberIdAndStatusAndChannel(UUID memberId, NotificationStatus status,
                                                                NotificationChannel channel, Pageable pageable) {
        return notificationJpaRepository.findByMemberIdAndStatusAndChannel(memberId, status, channel, pageable);
    }

    @Override
    public void save(Notification notification) {
        notificationJpaRepository.save(notification);
    }
}
