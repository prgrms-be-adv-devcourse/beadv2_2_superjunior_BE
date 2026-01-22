package store._0982.batch.domain.settlement;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.kafka.dto.SettlementDoneEvent;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "settlement", schema = "settlement_schema")
public class Settlement {

    @Id
    @Column(name = "settlement_id", nullable = false)
    private UUID settlementId;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SettlementStatus status;

    @Column(name = "service_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal serviceFee;

    @Column(name = "settlement_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal settlementAmount;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "bank_code")
    private String bankCode;

    public Settlement(
            UUID sellerId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            Long totalAmount,
            BigDecimal serviceFee,
            BigDecimal settlementAmount,
            String accountNumber,
            String bankCode
    ) {
        this.settlementId = UUID.randomUUID();
        this.sellerId = sellerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalAmount = totalAmount;
        this.serviceFee = serviceFee;
        this.settlementAmount = settlementAmount;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.status = SettlementStatus.PENDING;
    }

    public void markAsCompleted() {
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = OffsetDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == SettlementStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = SettlementStatus.FAILED;
    }

    public void markAsDeferred() {
        this.status = SettlementStatus.DEFERRED;
    }

    public void setAccountInfo(String accountNumber, String bankCode) {
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
    }

    public SettlementDoneEvent toCompletedEvent() {
        return toEvent(SettlementDoneEvent.Status.COMPLETED);
    }

    public SettlementDoneEvent toFailedEvent() {
        return toEvent(SettlementDoneEvent.Status.FAILED);
    }

    public SettlementDoneEvent toDeferredEvent() {
        return toEvent(SettlementDoneEvent.Status.DEFERRED);
    }

    private SettlementDoneEvent toEvent(SettlementDoneEvent.Status status) {
        return new SettlementDoneEvent(
                this.settlementId,
                this.sellerId,
                this.periodStart,
                this.periodEnd,
                status,
                this.totalAmount,
                this.serviceFee,
                this.settlementAmount
        );
    }
}
