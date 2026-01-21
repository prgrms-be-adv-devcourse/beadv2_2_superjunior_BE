package store._0982.member.infrastructure.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationStatus;

import java.util.List;
import java.util.UUID;

interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByMemberIdAndStatus(UUID memberId, NotificationStatus status);

    Page<Notification> findByMemberIdAndChannel(UUID memberId, NotificationChannel channel, Pageable pageable);

    Page<Notification> findByMemberIdAndStatusAndChannel(
            UUID memberId, NotificationStatus status, NotificationChannel channel, Pageable pageable);
}
