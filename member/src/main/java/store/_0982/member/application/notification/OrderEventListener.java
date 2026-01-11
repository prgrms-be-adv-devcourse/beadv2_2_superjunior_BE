package store._0982.member.application.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderChangedEvent;
import store._0982.common.log.ServiceLog;
import store._0982.member.common.notification.KafkaGroupIds;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.NotificationChannel;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.NotificationType;
import store._0982.member.exception.CustomKafkaException;

@Service
@RequiredArgsConstructor
public class OrderEventListener {
    private final NotificationRepository notificationRepository;

    // TODO: 주문의 상태를 어떤 단어로 정의했는지 확인해야 함
    @ServiceLog
    @RetryableTopic(exclude = CustomKafkaException.class)
    @KafkaListener(
            topics = {KafkaTopics.ORDER_CREATED, KafkaTopics.ORDER_CHANGED},
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppListenerContainerFactory"
    )
    public void handleOrderEvent(OrderChangedEvent event) {
        NotificationContent content = createNotificationContent(event);
        Notification notification = NotificationCreator.create(
                event,
                content,
                NotificationChannel.IN_APP
        );
        notificationRepository.save(notification);
    }

    private NotificationContent createNotificationContent(OrderChangedEvent event) {
        String productName = event.getProductName();
        return switch (event.getStatus()) {
            case CREATED -> new NotificationContent(
                    NotificationType.ORDER_SCHEDULED,
                    productName + " 공동 구매 신청 완료",
                    productName + " 상품의 공동 구매 신청이 완료되었습니다."
            );
            case SUCCESS -> new NotificationContent(
                    NotificationType.ORDER_COMPLETED,
                    productName + " 공동 구매 성공",
                    productName + " 상품의 공동 구매가 확정되어 곧 배송이 시작됩니다."
            );
            case FAILED -> new NotificationContent(
                    NotificationType.ORDER_FAILED,
                    productName + " 공동 구매 실패",
                    productName + " 상품의 공동 구매가 취소되어 곧 포인트가 반환됩니다."
            );
        };
    }
}
