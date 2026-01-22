package store._0982.member.application.notification;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import store._0982.member.application.notification.dto.OrderCompletedCommand;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.constant.NotificationType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NotificationContentCreator {

    public static NotificationContent orderCompleted(OrderCompletedCommand command) {
        String productName = command.productName();
        return new NotificationContent(
                NotificationType.ORDER_COMPLETED,
                "상품 구매 완료",
                String.format("상품 %s의 구매가 정상적으로 완료됐습니다.", productName),
                command.orderId()
        );
    }
}
