package store._0982.member.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.NotificationStatus;

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

    public Page<Notification> findByMemberId(UUID memberId, Pageable pageable) {
        return notificationJpaRepository.findByMemberId(memberId, pageable);
    }

    @Override
    public Page<Notification> findByMemberIdAndStatus(UUID memberId, NotificationStatus status, Pageable pageable) {
        return notificationJpaRepository.findByMemberIdAndStatus(memberId, status, pageable);
    }

    @Override
    public void save(Notification notification) {
        notificationJpaRepository.save(notification);
    }
}
