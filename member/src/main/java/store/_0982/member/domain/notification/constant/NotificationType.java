package store._0982.member.domain.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // [주문]
    ORDER_COMPLETED(ReferenceType.ORDER),
    ORDER_CANCELED(ReferenceType.ORDER),

    // [PG]
    PG_FAILED(ReferenceType.PG),
    PG_REFUNDED(ReferenceType.PG),

    // [포인트]
    POINT_CHARGED(ReferenceType.POINT),
    POINT_WITHDRAWN(ReferenceType.POINT),
    POINT_REFUNDED(ReferenceType.POINT),

    // [정산]
    SETTLEMENT_COMPLETED(ReferenceType.SETTLEMENT),
    SETTLEMENT_FAILED(ReferenceType.SETTLEMENT),
    SETTLEMENT_DEFERRED(ReferenceType.SETTLEMENT),

    // [공동 구매]
    GROUP_PURCHASE_SUCCESS(ReferenceType.GROUP_PURCHASE),
    GROUP_PURCHASE_FAILED(ReferenceType.GROUP_PURCHASE);

    private final ReferenceType referenceType;
}
