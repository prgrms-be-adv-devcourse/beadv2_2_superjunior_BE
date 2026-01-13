package store._0982.point.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import store._0982.point.domain.vo.PointAmount;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "point_balance", schema = "payment_schema")
public class PointBalance {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "member_id", nullable = false, unique = true, updatable = false)
    private UUID memberId;

    @Embedded
    private PointAmount pointAmount;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    public PointBalance(UUID memberId) {
        this.memberId = memberId;
        this.pointAmount = PointAmount.zero();
    }

    public void charge(long amount) {
        this.pointAmount = this.pointAmount.addPaid(amount);
    }

    public void earnBonus(long bonus) {
        this.pointAmount = this.pointAmount.addBonus(bonus);
    }

    public PointAmount use(long amount) {
        this.pointAmount = this.pointAmount.use(amount);
        lastUsedAt = OffsetDateTime.now();
        return pointAmount;
    }

    public void transfer(long amount) {
        this.pointAmount = this.pointAmount.use(amount);
    }

    public long getTotalBalance() {
        return pointAmount.getTotal();
    }

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
