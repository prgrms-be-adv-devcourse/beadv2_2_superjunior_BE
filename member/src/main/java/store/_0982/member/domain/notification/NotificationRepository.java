package store._0982.member.domain.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Optional<Notification> findById(UUID uuid);

    List<Notification> findByMemberIdAndStatus(UUID memberId, NotificationStatus status);

    Page<Notification> findByMemberId(UUID memberId, Pageable pageable);

    Page<Notification> findByMemberIdAndStatus(UUID memberId, NotificationStatus status, Pageable pageable);

    void save(Notification notification);
}
