package store._0982.common.domain.sellerpayout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seller_payout_failure", schema = "settlement_schema")
public class SellerPayoutFailure {

    @Id
    @Column(name = "failure_id", nullable = false, updatable = false)
    private UUID failureId;

    @Column(name = "seller_payout_id", nullable = false, updatable = false)
    private UUID sellerPayoutId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Column(name = "failure_reason", nullable = false, length = 500)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static SellerPayoutFailure createSellerPayoutFailure(
            UUID sellerPayoutId,
            UUID sellerId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            String failureReason,
            Integer retryCount
    ) {
        return new SellerPayoutFailure(
                sellerPayoutId,
                sellerId,
                periodStart,
                periodEnd,
                failureReason,
                retryCount
        );
    }

    private SellerPayoutFailure(
            UUID sellerPayoutId,
            UUID sellerId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            String failureReason,
            Integer retryCount
    ) {
        this.failureId = UUID.randomUUID();
        this.sellerPayoutId = sellerPayoutId;
        this.sellerId = sellerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
    }
}
