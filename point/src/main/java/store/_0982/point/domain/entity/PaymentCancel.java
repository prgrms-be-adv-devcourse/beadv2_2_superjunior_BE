package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "payment_cancel", schema = "point_schema")
public class PaymentCancel {

    @Id
    @Column(name = "cancel_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment; // 원본 결제

    @Column(name = "cancel_amount", nullable = false)
    private long cancelAmount; // 이번에 취소한 금액

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "pg_cancel_key")
    private String pgCancelKey; // PG사 취소 키

    @CreationTimestamp
    @Column(name = "canceled_at", nullable = false)
    private OffsetDateTime canceledAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
