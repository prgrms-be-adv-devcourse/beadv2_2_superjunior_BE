package store._0982.point.point.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_point", schema = "point_schema")
public class PaymentPoint {

    @Id
    @Column(name = "payment_point_id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "pg_order_id", nullable = false, unique = true)
    private UUID orderId;

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod;

    @Column(name = "payment_key", nullable = false, length = 50)
    private String paymentKey;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentPointStatus status;

    @Column(name = "fail_message")
    private String failMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "requested_at")
    private OffsetDateTime requestedAt;

    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    private PaymentPoint(UUID memberId, UUID orderId, int amount, OffsetDateTime requestedAt){
        this.id = UUID.randomUUID();
        this.memberId = memberId;
        this.orderId = orderId;
        this.amount = amount;
        this.requestedAt = requestedAt;
        this.status = PaymentPointStatus.REQUESTED;
    }

    public static PaymentPoint create(UUID memberId, UUID orderId, int amount, OffsetDateTime requestedAt){
        return new PaymentPoint(memberId, orderId, amount, requestedAt);
    }

    public void markConfirmed(String method, OffsetDateTime approvedAt, String paymentKey) {
        this.status = PaymentPointStatus.COMPLETED;
        this.paymentMethod = method;
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.failMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = PaymentPointStatus.FAILED;
        this.failMessage = errorMessage;
    }
}
