package store._0982.member.application.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store._0982.common.kafka.dto.*;
import store._0982.member.common.notification.NotificationContent;
import store._0982.member.domain.notification.*;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationCreatorTest {

    @Test
    @DisplayName("OrderEvent로 알림을 생성한다")
    void create_fromOrderEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        OrderChangedEvent event = new OrderChangedEvent(
                orderId,
                memberId,
                OrderChangedEvent.Status.CREATED,
                "테스트 상품"
        );
        NotificationContent content = new NotificationContent(
                NotificationType.ORDER_SCHEDULED,
                "주문 완료",
                "주문이 완료되었습니다."
        );

        // when
        Notification notification = NotificationCreator.create(event, content, NotificationChannel.IN_APP);

        // then
        assertThat(notification.getMemberId()).isEqualTo(memberId);
        assertThat(notification.getType()).isEqualTo(NotificationType.ORDER_SCHEDULED);
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(notification.getTitle()).isEqualTo("주문 완료");
        assertThat(notification.getMessage()).isEqualTo("주문이 완료되었습니다.");
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.ORDER);
        assertThat(notification.getReferenceId()).isEqualTo(orderId);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("PointEvent로 알림을 생성한다")
    void create_fromPointEvent() {
        // given
        UUID memberId = UUID.randomUUID();
        UUID pointId = UUID.randomUUID();
        PointChangedEvent event = new PointChangedEvent(
                pointId,
                memberId,
                10000L,
                PointChangedEvent.Status.CHARGED,
                "간편결제"
        );
        NotificationContent content = new NotificationContent(
                NotificationType.POINT_RECHARGED,
                "포인트 충전",
                "10,000원이 충전되었습니다."
        );

        // when
        Notification notification = NotificationCreator.create(event, content, NotificationChannel.IN_APP);

        // then
        assertThat(notification.getMemberId()).isEqualTo(memberId);
        assertThat(notification.getType()).isEqualTo(NotificationType.POINT_RECHARGED);
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(notification.getTitle()).isEqualTo("포인트 충전");
        assertThat(notification.getMessage()).isEqualTo("10,000원이 충전되었습니다.");
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.POINT);
        assertThat(notification.getReferenceId()).isEqualTo(pointId);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("SettlementEvent로 알림을 생성한다")
    void create_fromSettlementEvent() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID settlementId = UUID.randomUUID();
        SettlementDoneEvent event = new SettlementDoneEvent(
                settlementId,
                sellerId,
                java.time.OffsetDateTime.now().minusDays(1),
                java.time.OffsetDateTime.now(),
                SettlementDoneEvent.Status.COMPLETED,
                50000L,
                java.math.BigDecimal.valueOf(500),
                java.math.BigDecimal.valueOf(49500)
        );
        NotificationContent content = new NotificationContent(
                NotificationType.DAILY_SETTLEMENT_COMPLETED,
                "일일 정산 완료",
                "50,000원이 적립되었습니다."
        );

        // when
        Notification notification = NotificationCreator.create(event, content, NotificationChannel.IN_APP);

        // then
        assertThat(notification.getMemberId()).isEqualTo(sellerId);
        assertThat(notification.getType()).isEqualTo(NotificationType.DAILY_SETTLEMENT_COMPLETED);
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(notification.getTitle()).isEqualTo("일일 정산 완료");
        assertThat(notification.getMessage()).isEqualTo("50,000원이 적립되었습니다.");
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.SETTLEMENT);
        assertThat(notification.getReferenceId()).isEqualTo(settlementId);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    @DisplayName("GroupPurchaseChangedEvent로 알림을 생성한다")
    void create_fromGroupPurchaseChangedEvent() {
        // given
        UUID sellerId = UUID.randomUUID();
        UUID groupPurchaseId = UUID.randomUUID();
        GroupPurchaseChangedEvent event = new GroupPurchaseChangedEvent(
                groupPurchaseId,
                sellerId,
                "테스트 공동구매",
                GroupPurchaseChangedEvent.Status.SUCCESS,
                100000L
        );
        NotificationContent content = new NotificationContent(
                NotificationType.GROUP_PURCHASE_COMPLETED,
                "공동 구매 성사",
                "100,000원이 정산금에 추가됩니다."
        );

        // when
        Notification notification = NotificationCreator.create(event, content, NotificationChannel.IN_APP);

        // then
        assertThat(notification.getMemberId()).isEqualTo(sellerId);
        assertThat(notification.getType()).isEqualTo(NotificationType.GROUP_PURCHASE_COMPLETED);
        assertThat(notification.getChannel()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(notification.getTitle()).isEqualTo("공동 구매 성사");
        assertThat(notification.getMessage()).isEqualTo("100,000원이 정산금에 추가됩니다.");
        assertThat(notification.getReferenceType()).isEqualTo(ReferenceType.GROUP_PURCHASE);
        assertThat(notification.getReferenceId()).isEqualTo(groupPurchaseId);
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
    }
}
