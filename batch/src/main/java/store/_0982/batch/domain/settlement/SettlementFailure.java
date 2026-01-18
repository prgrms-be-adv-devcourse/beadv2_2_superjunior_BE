package store._0982.batch.domain.settlement;

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
@Table(name = "settlement_failure", schema = "settlement_schema")
public class SettlementFailure {

    @Id
    @Column(name = "failure_id", nullable = false)
    private UUID failureId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Column(name = "failure_reason", nullable = false, length = 500)
    private String failureReason;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "settlement_id", nullable = false)
    private UUID settlementId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public SettlementFailure(
            UUID sellerId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            String failureReason,
            Integer retryCount,
            UUID settlementId
    ) {
        this.failureId = UUID.randomUUID();
        this.sellerId = sellerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.failureReason = failureReason;
        this.retryCount = retryCount;
        this.settlementId = settlementId;
    }

}
