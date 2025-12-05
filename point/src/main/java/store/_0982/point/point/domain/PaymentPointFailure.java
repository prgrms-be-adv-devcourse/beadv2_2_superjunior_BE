package store._0982.point.point.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "payment_point_failure", schema = "point_schema")
public class PaymentPointFailure {

    @Id
    @Column(name = "failure_id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "payment_point_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "payment_point_failure_payment_point_payment_point_id_fk")
    )
    private PaymentPoint paymentPoint;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "payment_key", nullable = false, length = 50)
    private String paymentKey;

    @Column(name = "error_code", length = 30)
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "amount")
    private Integer amount;

    @Lob
    @Column(name = "raw_payload", nullable = false)
    private String rawPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected PaymentPointFailure(){}

    private PaymentPointFailure(
            UUID orderId,
            String paymentKey,
            String errorCode,
            String errorMessage,
            int amount,
            String rawPayload
    ){
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.amount = amount;
        this.rawPayload = rawPayload;
    }

    public static PaymentPointFailure from(UUID orderId,
                                           String paymentKey,
                                           String errorCode,
                                           String errorMessage,
                                           int amount,
                                           String rawPayload) {
        return new PaymentPointFailure(orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}