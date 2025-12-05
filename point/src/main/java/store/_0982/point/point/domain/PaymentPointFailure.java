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

    @Column(name = "payment_point_id", nullable = false)
    private UUID paymentPointId;

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

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected PaymentPointFailure(){}

    private PaymentPointFailure(
            UUID paymentPointId,
            UUID orderId,
            String paymentKey,
            String errorCode,
            String errorMessage,
            int amount,
            String rawPayload
    ){
        this.id = UUID.randomUUID();
        this.paymentPointId = paymentPointId;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.amount = amount;
        this.rawPayload = rawPayload;
    }

    public static PaymentPointFailure from(UUID paymentPointId,
                                           UUID orderId,
                                           String paymentKey,
                                           String errorCode,
                                           String errorMessage,
                                           int amount,
                                           String rawPayload) {
        return new PaymentPointFailure(paymentPointId, orderId, paymentKey, errorCode, errorMessage, amount, rawPayload);
    }
}