package store._0982.notification.application;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.PointEvent;
import store._0982.common.log.ServiceLog;
import store._0982.notification.common.KafkaGroupIds;
import store._0982.notification.common.NotificationContent;
import store._0982.notification.domain.Notification;
import store._0982.notification.domain.NotificationChannel;
import store._0982.notification.domain.NotificationRepository;
import store._0982.notification.domain.NotificationType;

@Service
@RequiredArgsConstructor
public class PointEventService {
    private final NotificationRepository notificationRepository;

    @ServiceLog
    @RetryableTopic
    @KafkaListener(
            topics = {KafkaTopics.POINT_CHANGED, KafkaTopics.POINT_RECHARGED},
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppConsumerFactory"
    )
    public void handlePointChangedEvent(PointEvent event) {
        MDC.put("eventId", String.valueOf(event.getEventId()));
        NotificationContent content = createNotificationContent(event);
        Notification notification = NotificationCreator.create(
                event,
                content.type(),
                content.title(),
                content.message(),
                NotificationChannel.IN_APP
        );
        notificationRepository.save(notification);
    }

    private NotificationContent createNotificationContent(PointEvent event) {
        return switch (event.getStatus()) {
            case RECHARGED -> new NotificationContent(
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
