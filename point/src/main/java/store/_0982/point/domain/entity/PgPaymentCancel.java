package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "pg_payment_cancel", schema = "payment_schema")
public class PgPaymentCancel {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private PgPayment pgPayment;

    @Column(name = "cancel_amount", nullable = false)
    private long cancelAmount;

    @Column(nullable = false)
    private long remainingAmount;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "payment_key", nullable = false, unique = true, updatable = false)
    private String paymentKey;

    @Column(nullable = false, updatable = false)
    private String transactionKey;

    @Column(name = "canceled_at", nullable = false)
    private OffsetDateTime canceledAt;

    public static PgPaymentCancel from(
            PgPayment pgPayment,
            String cancelReason,
            long cancelAmount,
            OffsetDateTime canceledAt,
            String transactionKey
    ) {
        return PgPaymentCancel.builder()
                .pgPayment(pgPayment)
                .paymentKey(pgPayment.getPaymentKey())
                .cancelReason(cancelReason)
                .cancelAmount(cancelAmount)
                .canceledAt(canceledAt)
                .transactionKey(transactionKey)
                .build();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (canceledAt == null) {
            canceledAt = OffsetDateTime.now();
        }
    }
}
