package store._0982.common.domain.settlement;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.kafka.dto.SellerPayoutDoneEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seller_payout", schema = "settlement_schema")
public class SellerPayout {

    @Id
    @Column(name = "seller_payout_id", nullable = false, updatable = false)
    private UUID sellerPayoutId;

    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "period_start", nullable = false)
    private OffsetDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private OffsetDateTime periodEnd;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SellerPayoutStatus status;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "bank_code")
    private String bankCode;

    public void markAsCompleted() {
        this.status = SellerPayoutStatus.COMPLETED;
        this.paidAt = OffsetDateTime.now();
    }

    public boolean isCompleted() {
        return this.status == SellerPayoutStatus.COMPLETED;
    }

    public void markAsFailed() {
        this.status = SellerPayoutStatus.FAILED;
    }

    public void markAsDeferred() {
        this.status = SellerPayoutStatus.DEFERRED;
    }

    public void setAccountInfo(String accountNumber, String bankCode) {
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
    }

    public SellerPayoutDoneEvent toCompletedEvent() {
        return toEvent(SellerPayoutDoneEvent.Status.COMPLETED);
    }

    public SellerPayoutDoneEvent toFailedEvent() {
        return toEvent(SellerPayoutDoneEvent.Status.FAILED);
    }

    public SellerPayoutDoneEvent toDeferredEvent() {
        return toEvent(SellerPayoutDoneEvent.Status.DEFERRED);
    }

    private SellerPayoutDoneEvent toEvent(SellerPayoutDoneEvent.Status status) {
        return new SellerPayoutDoneEvent(
                this.sellerPayoutId,
                this.sellerId,
                this.periodStart,
                this.periodEnd,
                status,
                this.totalAmount
        );
    }

    public static SellerPayout createSellerPayout(
            UUID sellerId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            Long totalAmount,
            String accountNumber,
            String bankCode
    ) {
        return new SellerPayout(
                sellerId,
                periodStart,
                periodEnd,
                totalAmount,
                accountNumber,
                bankCode
        );
    }

    private SellerPayout(
            UUID sellerId,
            OffsetDateTime periodStart,
            OffsetDateTime periodEnd,
            Long totalAmount,
            String accountNumber,
            String bankCode
    ) {
        this.sellerPayoutId = UUID.randomUUID();
        this.sellerId = sellerId;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalAmount = totalAmount;
        this.accountNumber = accountNumber;
        this.bankCode = bankCode;
        this.status = SellerPayoutStatus.PENDING;
    }
}
