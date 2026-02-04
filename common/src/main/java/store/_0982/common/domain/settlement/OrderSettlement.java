package store._0982.common.domain.settlement;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_settlement", schema = "settlement_schema")
public class OrderSettlement {

    @Id
    @Column(name = "order_settlement_id", nullable = false)
    private UUID orderSettlementId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "group_purchase_id", nullable = false, updatable = false)
    private UUID groupPurchaseId;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_settlement_status", nullable = false)
    private OrderSettlementStatus orderSettlementStatus;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    public static OrderSettlement createOrderSettlement(
            UUID orderId,
            UUID sellerId,
            UUID groupPurchaseId,
            Long totalAmount,
            OrderSettlementStatus orderSettlementStatus
    ) {
        return new OrderSettlement(
                orderId,
                sellerId,
                groupPurchaseId,
                totalAmount,
                orderSettlementStatus
        );
    }

    private OrderSettlement(
            UUID orderId,
            UUID sellerId,
            UUID groupPurchaseId,
            Long totalAmount,
            OrderSettlementStatus orderSettlementStatus
    ) {
        this.orderSettlementId = UUID.randomUUID();
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.groupPurchaseId = groupPurchaseId;
        this.totalAmount = totalAmount;
        this.orderSettlementStatus = orderSettlementStatus;
    }
}
