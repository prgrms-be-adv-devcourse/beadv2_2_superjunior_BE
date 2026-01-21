package store._0982.member.application.notification.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Service;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.SettlementDoneEvent;
import store._0982.common.log.ServiceLog;
import store._0982.member.application.notification.NotificationCreator;
import store._0982.member.common.notification.KafkaGroupIds;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.Notification;
import store._0982.member.domain.notification.constant.NotificationChannel;
import store._0982.member.domain.notification.NotificationRepository;
import store._0982.member.domain.notification.constant.NotificationType;
import store._0982.member.exception.CustomKafkaException;

import java.text.DecimalFormat;

@Service
@RequiredArgsConstructor
public class SettlementEventListener {
    private static final DecimalFormat AMOUNT_FORMATTER = new DecimalFormat("#,###");

    private final NotificationRepository notificationRepository;

    @ServiceLog
    @RetryableTopic(exclude = CustomKafkaException.class)
    @KafkaListener(
            topics = {KafkaTopics.SETTLEMENT_DONE},
            groupId = KafkaGroupIds.IN_APP,
            containerFactory = "inAppListenerContainerFactory"
    )
    public void handleMonthlySettlementEvent(SettlementDoneEvent event) {
        NotificationContent content = createMonthlyContent(event);
        Notification notification = NotificationCreator.create(event, content, NotificationChannel.IN_APP);
        notificationRepository.save(notification);
    }

    private NotificationContent createMonthlyContent(SettlementDoneEvent event) {
        return switch (event.getStatus()) {
            case COMPLETED -> new NotificationContent(
                    NotificationType.MONTHLY_SETTLEMENT_COMPLETED,
                    "월간 정산 완료",
                    String.format("월간 정산이 완료되어 %s원이 정상적으로 송금되었습니다.",
                            AMOUNT_FORMATTER.format(event.getSettlementAmount())));
            case FAILED -> new NotificationContent(
                    NotificationType.MONTHLY_SETTLEMENT_FAILED,
                    "월간 정산 실패",
                    String.format("월간 정산에 실패해 %s원이 송금되지 않았습니다. 오류가 반복될 경우 관리자에게 문의하세요.",
                            AMOUNT_FORMATTER.format(event.getSettlementAmount())));
            case DEFERRED -> new NotificationContent(
                    NotificationType.MONTHLY_SETTLEMENT_COMPLETED,
                    "월간 정산 보류",
                    String.format("월간 정산 금액 %s원이 최소 송금 기준에 미달하여 다음 달에 합산 송금됩니다.",
                            AMOUNT_FORMATTER.format(event.getSettlementAmount())));
        };
    }
}
