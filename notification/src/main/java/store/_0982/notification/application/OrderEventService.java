package store._0982.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.OrderEvent;
import store._0982.common.log.ServiceLog;
import store._0982.notification.constant.KafkaGroupIds;
import store._0982.notification.domain.*;
import store._0982.notification.exception.CustomKafkaException;
import store._0982.notification.exception.KafkaErrorCode;

@Service
@RequiredArgsConstructor
public class OrderEventService {
    private final NotificationRepository notificationRepository;

    // TODO: 주문의 상태를 어떤 단어로 정의했는지 확인해야 함
    @ServiceLog
    @RetryableTopic(exclude = CustomKafkaException.class)
    @KafkaListener(
            topics = {KafkaTopics.ORDER_CREATED, KafkaTopics.ORDER_STATUS_CHANGED},
            groupId = KafkaGroupIds.ORDER_NOTIFICATION,
            containerFactory = "orderListenerContainerFactory"
    )
    public void handleOrderCreated(OrderEvent event) {
        Notification notification = switch (event.getStatus().toUpperCase()) {
            case "OPEN" -> created(event);
            case "SUCCESS" -> completed(event);
            case "FAILED" -> canceled(event);
            default -> throw new CustomKafkaException(KafkaErrorCode.KAFKA_INVALID_EVENT);
        };
        notificationRepository.save(notification);
    }

    private static Notification created(OrderEvent event) {
        String productName = event.getProductName();
        String title = productName + " 공동 구매 신청 완료";
        String message = productName + " 상품의 공동 구매 신청이 완료되었습니다.";
        NotificationType type = NotificationType.ORDER_SCHEDULED;
        return createInApp(event, type, title, message);
    }

    private static Notification completed(OrderEvent event) {
        String productName = event.getProductName();
        String title = productName + " 공동 구매 성공";
        String message = productName + " 상품의 공동 구매가 확정되어 곧 배송이 시작됩니다.";
        NotificationType type = NotificationType.ORDER_COMPLETED;
        return createInApp(event, type, title, message);
    }

    private static Notification canceled(OrderEvent event) {
        String productName = event.getProductName();
        String title = productName + " 공동 구매 실패";
        String message = productName + " 상품의 공동 구매가 취소되어 곧 포인트가 반환됩니다.";
        NotificationType type = NotificationType.ORDER_FAILED;
        return createInApp(event, type, title, message);
    }

    private static Notification createInApp(OrderEvent event, NotificationType type, String title, String message) {
        return Notification.builder()
                .memberId(event.getMemberId())
                .type(type)
                .channel(NotificationChannel.IN_APP)
                .title(title)
                .message(message)
                .referenceType(ReferenceType.ORDER)
                .status(NotificationStatus.SENT)
                .referenceId(event.getId())
                .build();
    }
}
