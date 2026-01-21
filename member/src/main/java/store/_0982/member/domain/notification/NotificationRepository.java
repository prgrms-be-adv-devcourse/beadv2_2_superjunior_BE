package store._0982.member.domain.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Optional<Notification> findById(UUID uuid);

    List<Notification> findByMemberIdAndStatus(UUID memberId, NotificationStatus status);

    Page<Notification> findByMemberIdAndChannel(UUID memberId, NotificationChannel channel, Pageable pageable);

    Page<Notification> findByMemberIdAndStatusAndChannel(UUID memberId, NotificationStatus status, NotificationChannel channel, Pageable pageable);

    void save(Notification notification);
}
