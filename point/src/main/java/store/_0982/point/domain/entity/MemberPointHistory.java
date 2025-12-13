package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import store._0982.point.application.dto.PointDeductCommand;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.domain.constant.MemberPointHistoryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "member_point_history",
        schema = "point_schema",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "member_point_history_pk_3",
                        columnNames = {"order_id", "status"}
                )
        }
)
public class MemberPointHistory {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, updatable = false)
    private MemberPointHistoryStatus status;

    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @CreationTimestamp
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "idempotency_key", nullable = false, updatable = false, unique = true)
    private UUID idempotencyKey;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    public static MemberPointHistory used(UUID memberId, PointDeductCommand command) {
        return MemberPointHistory.builder()
                .memberId(memberId)
                .idempotencyKey(command.idempotencyKey())
                .status(MemberPointHistoryStatus.USED)
                .amount(command.amount())
                .orderId(command.orderId())
                .build();
    }

    public static MemberPointHistory returned(UUID memberId, PointReturnCommand command) {
        return MemberPointHistory.builder()
                .memberId(memberId)
                .idempotencyKey(command.idempotencyKey())
                .status(MemberPointHistoryStatus.RETURNED)
                .amount(command.amount())
                .orderId(command.orderId())
                .build();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
