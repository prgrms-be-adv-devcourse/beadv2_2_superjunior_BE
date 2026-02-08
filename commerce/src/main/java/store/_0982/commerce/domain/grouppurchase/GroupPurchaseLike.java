package store._0982.commerce.domain.grouppurchase;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "\"group_purchase_like\"", schema = "product_schema",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "group_purchase_id"})
)
public class GroupPurchaseLike {
    @Id
    private UUID groupPurchaseLikeId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "group_purchase_id", nullable = false)
    private UUID groupPurchaseId;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    public GroupPurchaseLike(UUID memberId, UUID groupPurchaseId) {
        this.groupPurchaseLikeId = UUID.randomUUID();
        this.memberId = memberId;
        this.groupPurchaseId = groupPurchaseId;
    }
}
