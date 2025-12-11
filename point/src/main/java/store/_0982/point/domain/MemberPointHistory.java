package store._0982.point.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member_point_history", schema = "point_schema")
public class MemberPointHistory {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberPointHistoryStatus status;

    @Column(name = "amount", nullable = false)
    private long amount;

    @CreationTimestamp
    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public static MemberPointHistory used(UUID memberId, long amount) {
        return MemberPointHistory.builder()
                .memberId(memberId)
                .status(MemberPointHistoryStatus.USED)
                .amount(amount)
                .build();
    }

    public void markReturned() {
        this.status = MemberPointHistoryStatus.RETURNED;
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
