package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import store._0982.point.application.dto.PgFailCommand;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "pg_payment_failure", schema = "payment_schema")
public class PgPaymentFailure {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PgPayment pgPayment;

    @Column(name = "payment_key", nullable = false, unique = true)
    private String paymentKey;

    @Column(name = "error_code", length = 30)
    private String errorCode;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "raw_payload", nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public static PgPaymentFailure systemError(PgPayment pgPayment) {
        return PgPaymentFailure.builder()
                .pgPayment(pgPayment)
                .errorCode("SYSTEM_ERROR")
                .errorMessage("system error")
                .paymentKey(pgPayment.getPaymentKey())
                .amount(pgPayment.getAmount())
                .build();
    }

    public static PgPaymentFailure pgError(PgPayment pgPayment, PgFailCommand command) {
        return PgPaymentFailure.builder()
                .pgPayment(pgPayment)
                .errorCode(command.errorCode())
                .errorMessage(command.errorMessage())
                .paymentKey(command.paymentKey())
                .amount(command.amount())
                .rawPayload(command.rawPayload())
                .build();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
