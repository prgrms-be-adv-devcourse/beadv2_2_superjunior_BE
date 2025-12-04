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

    @OneToOne
    @JoinColumn(name = "payment_point_id", nullable = false, unique = true)
    private PaymentPoint paymentPoint;

    @Column(name = "payment_key", nullable = false, length = 50)
    private String paymentKey;

    @Column(name = "error_code", length = 30)
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column
    private Integer amount;

    @Column(name = "raw_payload", nullable = false)
    private String rawPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}