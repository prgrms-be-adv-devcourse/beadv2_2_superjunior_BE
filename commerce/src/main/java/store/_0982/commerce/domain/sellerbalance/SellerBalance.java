package store._0982.commerce.domain.sellerbalance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "seller_balance", schema = "order_schema")
public class SellerBalance {

    @Id
    @Column(name = "balance_id", nullable = false)
    private UUID balanceId;

    @Column(name = "member_id", nullable = false, unique = true)
    private UUID memberId;

    @Column(name = "settlement_balance", nullable = false)
    private Long settlementBalance;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public SellerBalance(UUID sellerId) {
        this.balanceId = UUID.randomUUID();
        this.memberId = sellerId;
        this.settlementBalance = 0L;
    }

    public void increaseBalance(Long amount) {
        if (amount < 0)
            throw new CustomException(CustomErrorCode.INVALID_SETTLEMENT_AMOUNT);
        this.settlementBalance += amount;
    }

}
