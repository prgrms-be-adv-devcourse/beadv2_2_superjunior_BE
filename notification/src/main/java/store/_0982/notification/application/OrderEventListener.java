package store._0982.notification.application;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderEvent;
import store._0982.common.log.ServiceLog;
import store._0982.notification.common.KafkaGroupIds;
import store._0982.notification.common.NotificationContent;
import store._0982.notification.domain.Notification;
import store._0982.notification.domain.NotificationChannel;
import store._0982.notification.domain.NotificationRepository;
import store._0982.notification.domain.NotificationType;
import store._0982.notification.exception.CustomKafkaException;
import store._0982.notification.exception.KafkaErrorCode;

@Service
@RequiredArgsConstructor
public class OrderEventListener {
    private final NotificationRepository notificationRepository;

    // TODO: 주문의 상태를 어떤 단어로 정의했는지 확인해야 함
    @ServiceLog
    @RetryableTopic(exclude = CustomKafkaException.class)
    @KafkaListener(
            topics = {KafkaTopics.ORDER_CREATED, KafkaTopics.ORDER_STATUS_CHANGED},
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppListenerContainerFactory"
    )
    public void handleOrderEvent(OrderEvent event) {
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

    private NotificationContent createNotificationContent(OrderEvent event) {
        String productName = event.getProductName();
        return switch (event.getStatus().toUpperCase()) {
            case "OPEN" -> new NotificationContent(
                    NotificationType.POINT_RECHARGED,
                    productName + " 공동 구매 신청 완료",
                    productName + " 상품의 공동 구매 신청이 완료되었습니다."
            );
            case "SUCCESS" -> new NotificationContent(
                    NotificationType.POINT_DEDUCTED,
                    productName + " 공동 구매 성공",
                    productName + " 상품의 공동 구매가 확정되어 곧 배송이 시작됩니다."
            );
            case "FAILED" -> new NotificationContent(
                    NotificationType.POINT_RETURNED,
                    productName + " 공동 구매 실패",
                    productName + " 상품의 공동 구매가 취소되어 곧 포인트가 반환됩니다."
            );
            default -> throw new CustomKafkaException(KafkaErrorCode.KAFKA_INVALID_EVENT);
        };
    }
}
