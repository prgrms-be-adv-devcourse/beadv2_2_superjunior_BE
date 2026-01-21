package store._0982.member.application.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointChangedEvent;
import store._0982.common.log.ServiceLog;
import store._0982.member.application.notification.NotificationCreator;
import store._0982.member.common.notification.KafkaGroupIds;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationType;

@Service
@RequiredArgsConstructor
public class PointEventListener {
    private final NotificationRepository notificationRepository;

    @ServiceLog
    @RetryableTopic
    @KafkaListener(
            topics = {KafkaTopics.POINT_CHANGED},
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppListenerContainerFactory"
    )
    public void handlePointChangedEvent(PointChangedEvent event) {
        NotificationContent content = createNotificationContent(event);
        Notification notification = NotificationCreator.create(
                event,
                content,
                NotificationChannel.IN_APP
        );
        notificationRepository.save(notification);
    }

    private NotificationContent createNotificationContent(PointChangedEvent event) {
        return switch (event.getStatus()) {
            case CHARGED -> new NotificationContent(
                    NotificationType.POINT_RECHARGED,
                    "포인트 충전 완료",
                    String.format("포인트 %,d원이 정상적으로 충전되었습니다.", event.getAmount())
            );
            case DEDUCTED -> new NotificationContent(
                    NotificationType.POINT_DEDUCTED,
                    "포인트 차감 완료",
                    String.format("포인트 %,d원이 정상적으로 차감되었습니다.", event.getAmount())
            );
            case RETURNED -> new NotificationContent(
                    NotificationType.POINT_RETURNED,
                    "포인트 반환 완료",
                    String.format("포인트 %,d원이 정상적으로 반환되었습니다.", event.getAmount())
            );
        };
    }
}
