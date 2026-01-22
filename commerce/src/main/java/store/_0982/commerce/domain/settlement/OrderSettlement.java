package store._0982.commerce.domain.settlement;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import store._0982.commerce.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_settlement", schema = "settlement_schema")
public class OrderSettlement {

    @Id
    @Column(name = "order_settlement_id", nullable = false)
    private UUID orderSettlementId;

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "group_purchase_id", nullable = false, updatable = false)
    private UUID groupPurchaseId;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus;

    @Column(name = "settlement_id")
    private UUID settlementId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "settled_id")
    private OffsetDateTime settledAt;
}
