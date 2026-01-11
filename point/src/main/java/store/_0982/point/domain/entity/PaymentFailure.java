package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import store._0982.point.application.dto.PaymentFailCommand;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "payment_failure", schema = "payment_schema")
public class PaymentFailure {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

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

    public static PaymentFailure systemError(Payment payment) {
        return PaymentFailure.builder()
                .payment(payment)
                .errorCode("SYSTEM_ERROR")
                .errorMessage("system error")
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getAmount())
                .build();
    }

    public static PaymentFailure pgError(Payment payment, PaymentFailCommand command) {
        return PaymentFailure.builder()
                .payment(payment)
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
