package store._0982.member.infrastructure.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.log.ServiceLog;
import store._0982.member.application.notification.NotificationCreator;
import store._0982.member.common.notification.KafkaGroupIds;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationType;
import store._0982.member.exception.CustomKafkaException;

@Service
@RequiredArgsConstructor
public class GroupPurchaseEventListener {
    private final NotificationRepository notificationRepository;

    @ServiceLog
    @RetryableTopic(exclude = CustomKafkaException.class)
    @KafkaListener(
            topics = KafkaTopics.GROUP_PURCHASE_CHANGED,
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppListenerContainerFactory"
    )
    public void handleGroupPurchaseChangedEvent(GroupPurchaseEvent event) {
        NotificationContent content = createContent(event);
        if (content == null) {
            return;
        }
        Notification notification = NotificationCreator.create(event, content, NotificationChannel.IN_APP);
        notificationRepository.save(notification);
    }

    // TODO: 나중에 제대로 알림 분기 및 내용 수정
    private NotificationContent createContent(GroupPurchaseEvent event) {
        return switch (event.getGroupPurchaseStatus()) {
            case SCHEDULED, OPEN -> null;
            case SUCCESS -> new NotificationContent(
                    NotificationType.GROUP_PURCHASE_COMPLETED,
                    "공동 구매 성사 완료",
                    String.format("공동 구매가 성사되어 %,d원이 정산금에 추가될 예정입니다.", event.getDiscountedPrice()));
            case FAILED -> new NotificationContent(
                    NotificationType.GROUP_PURCHASE_FAILED,
                    "공동 구매 성사 실패",
                    "공동 구매 성사 조건을 만족하지 못해 공동 구매가 성사되지 않았습니다.");

        };
    }
}
