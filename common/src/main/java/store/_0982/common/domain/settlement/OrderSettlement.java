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

    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_settlement_status", nullable = false)
    private OrderSettlementStatus status;

    @Column(name = "order_amount", nullable = false)       // 주문 금액 (수량 * 할인가)
    private Long orderAmount;

    @Column(name = "platform_fee_rate", nullable = false)  // 플랫폼 수수료율 (예: 5%)
    private Double platformFeeRate;

    @Column(name = "platform_fee", nullable = false)       // 플랫폼 수수료액
    private Long platformFee;

    @Column(name = "settlement_amount", nullable = false)  // 판매자 정산액
    private Long settlementAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    public static OrderSettlement createConfirmedOrderSettlement(
            UUID orderId,
            UUID sellerId,
            UUID groupPurchaseId,
            Long orderAmount,
            Double platformFeeRate
    ) {
        Long platformFee = (long) (orderAmount * platformFeeRate);
        Long settlementAmount = orderAmount - platformFee;

        return new OrderSettlement(
                orderId,
                sellerId,
                groupPurchaseId,
                orderAmount,
                platformFeeRate,
                platformFee,
                settlementAmount,
                OrderSettlementStatus.COMPLETED
        );
    }

    public static OrderSettlement createCanceledOrderSettlement(
            UUID orderId,
            UUID sellerId,
            UUID groupPurchaseId,
            Long cancelFee,  // 취소 수수료만 판매자에게 지급
            OrderSettlementStatus status
    ) {
        return new OrderSettlement(
                orderId,
                sellerId,
                groupPurchaseId,
                0L,
                0.0,
                0L,
                cancelFee,  // settlementAmount = 취소 수수료만
                status
        );
    }

    private OrderSettlement(
            UUID orderId,
            UUID sellerId,
            UUID groupPurchaseId,
            Long orderAmount,
            Double platformFeeRate,
            Long platformFee,
            Long settlementAmount,
            OrderSettlementStatus status
    ) {
        this.orderSettlementId = UUID.randomUUID();
        this.orderId = orderId;
        this.sellerId = sellerId;
        this.groupPurchaseId = groupPurchaseId;
        this.orderAmount = orderAmount;
        this.platformFeeRate = platformFeeRate;
        this.platformFee = platformFee;
        this.settlementAmount = settlementAmount;
        this.status = status;
    }
}
