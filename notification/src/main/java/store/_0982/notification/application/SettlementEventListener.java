package store._0982.notification.application;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementEvent;
import store._0982.common.log.ServiceLog;
import store._0982.notification.common.KafkaGroupIds;
import store._0982.notification.common.NotificationContent;
import store._0982.notification.domain.Notification;
import store._0982.notification.domain.NotificationChannel;
import store._0982.notification.domain.NotificationRepository;
import store._0982.notification.domain.NotificationType;
import store._0982.notification.exception.CustomKafkaException;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
public class SettlementEventListener {
    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,###");

    private final NotificationRepository notificationRepository;

    @ServiceLog
    @RetryableTopic(exclude = CustomKafkaException.class)
    @KafkaListener(
            topics = {KafkaTopics.DAILY_SETTLEMENT_COMPLETED, KafkaTopics.DAILY_SETTLEMENT_FAILED},
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppListenerContainerFactory"
    )
    public void handleDailySettlementEvent(SettlementEvent event) {
        NotificationContent content = createDailyContent(event);
        Notification notification = NotificationCreator.create(event, content, NotificationChannel.IN_APP);
        notificationRepository.save(notification);
    }

    @ServiceLog
    @RetryableTopic(exclude = CustomKafkaException.class)
    @KafkaListener(
            topics = {KafkaTopics.MONTHLY_SETTLEMENT_COMPLETED, KafkaTopics.MONTHLY_SETTLEMENT_FAILED},
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppListenerContainerFactory"
    )
    public void handleMonthlySettlementEvent(SettlementEvent event) {
        NotificationContent content = createMonthlyContent(event);
        Notification notification = NotificationCreator.create(event, content, NotificationChannel.IN_APP);
        notificationRepository.save(notification);
    }

    private NotificationContent createDailyContent(SettlementEvent event) {
        return switch (event.getStatus()) {
            case SUCCESS -> new NotificationContent(
                    NotificationType.DAILY_SETTLEMENT_COMPLETED,
                    "일일 정산 완료",
                    String.format("일일 정산이 정상적으로 완료되어 %s원이 적립되었습니다.",
                            AMOUNT_FORMATTER.format(event.getSettlementAmount())));
            case FAILED -> new NotificationContent(
                    NotificationType.DAILY_SETTLEMENT_FAILED,
                    "일일 정산 실패",
                    String.format("일일 정산이 실패해 %s원이 다음날 같이 적립될 예정입니다.",
                            AMOUNT_FORMATTER.format(event.getSettlementAmount())));
        };
    }

    private NotificationContent createMonthlyContent(SettlementEvent event) {
        return switch (event.getStatus()) {
            case SUCCESS -> new NotificationContent(
                    NotificationType.MONTHLY_SETTLEMENT_COMPLETED,
                    "월간 정산 완료",
                    String.format("월간 정산이 완료되어 %s원이 정상적으로 송금되었습니다.",
                            AMOUNT_FORMATTER.format(event.getSettlementAmount())));
            case FAILED -> new NotificationContent(
                    NotificationType.MONTHLY_SETTLEMENT_FAILED,
                    "월간 정산 실패",
                    String.format("월간 정산에 실패해 %s원이 송금되지 않았습니다. 오류가 반복될 경우 관리자에게 문의하세요.",
                            AMOUNT_FORMATTER.format(event.getSettlementAmount())));
        };
    }
}
