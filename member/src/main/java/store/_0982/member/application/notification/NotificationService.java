package store._0982.member.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.member.application.notification.dto.NotificationInfo;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.constant.NotificationStatus;
import store._0982.member.exception.CustomErrorCode;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @ServiceLog
    @Transactional
    public void read(UUID memberId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOTIFICATION_NOT_FOUND));

        notification.validateMemberId(memberId);
        notification.read();
    }

    @ServiceLog
    @Transactional
    public void readAll(UUID memberId) {
        notificationRepository.findByMemberIdAndStatus(memberId, NotificationStatus.SENT)
                .forEach(Notification::read);
    }

    @Transactional
    public void saveNotification(Notifiable notifiable, NotificationChannel channel) {
        Notification notification = Notification.from(notifiable.content(), channel, notifiable.memberId());
        notificationRepository.save(notification);
    }

    @Transactional
    public void saveBulkNotifications(List<Notifiable> notifiables, NotificationChannel channel) {
        List<Notification> notifications = notifiables.stream()
                .map(notifiable -> Notification.from(notifiable.content(), channel, notifiable.memberId()))
                .toList();

        notificationRepository.saveAll(notifications);
    }

    public PageResponse<NotificationInfo> getNotifications(UUID memberId, Pageable pageable) {
        return PageResponse.from(notificationRepository.findByMemberIdAndChannel(memberId, NotificationChannel.IN_APP, pageable)
                .map(NotificationInfo::from));
    }

    public PageResponse<NotificationInfo> getUnreadNotifications(UUID memberId, Pageable pageable) {
        return PageResponse.from(notificationRepository
                .findByMemberIdAndStatusAndChannel(memberId, NotificationStatus.SENT, NotificationChannel.IN_APP, pageable)
                .map(NotificationInfo::from));
    }
}
