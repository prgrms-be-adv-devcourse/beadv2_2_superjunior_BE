package store._0982.member.infrastructure.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.notification.domain.notification.Notification;
import store._0982.notification.domain.notification.NotificationStatus;

import java.util.List;
import java.util.UUID;

interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByMemberIdAndStatus(UUID memberId, NotificationStatus status);

    Page<Notification> findByMemberId(UUID memberId, Pageable pageable);

    Page<Notification> findByMemberIdAndStatus(UUID memberId, NotificationStatus status, Pageable pageable);
}
