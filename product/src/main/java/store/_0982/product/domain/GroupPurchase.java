package store._0982.product.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "\"group_purchase\"")
public class GroupPurchase {
    @Id
    private UUID groupPurchaseId;

    @Column(name = "min_quantity", nullable = false)
    private int minQuantity;

    @Column(name = "max_quantity")
    private int maxQuantity;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "discounted_price", nullable = false)
    private int discountedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupPurchaseStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "current_quantity", nullable = false)
    private int currentQuantity = 0;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
    
    public GroupPurchase(int mintQuantity,
                         int maxQuantity,
                         String title,
                         String description,
                         int discountedPrice,
                         LocalDateTime startDate,
                         LocalDate endDate,
                         UUID sellerId,
                         UUID productId){
        this.groupPurchaseId = UUID.randomUUID();
        this.minQuantity = mintQuantity;
        this.maxQuantity = maxQuantity;
        this.title = title;
        this.description = description;
        this.discountedPrice = discountedPrice;
        this.status = GroupPurchaseStatus.SCHEDULED;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sellerId = sellerId;
        this.productId = productId;
        this.currentQuantity = 0;
    }

    public int getRemainingQuantity() {
        return this.maxQuantity - this.currentQuantity;
    }

    public boolean increaseQuantity(int quantity) {
        if (!canParticipate(quantity)) {
            return false;
        }

        this.currentQuantity += quantity;
        checkAndUpdateStatusIfMaxReached();

        return true;
    }

    private boolean canParticipate(int quantity) {
        return status == GroupPurchaseStatus.OPEN
                && (this.currentQuantity + quantity <= this.maxQuantity);
    }

    private void checkAndUpdateStatusIfMaxReached() {
        if (this.currentQuantity == this.maxQuantity) {
            this.status = GroupPurchaseStatus.SUCCESS;
        }
    }

    public void updateGroupPurchase(int mintQuantity,
                                    int maxQuantity,
                                    String title,
                                    String description,
                                    int discountedPrice,
                                    LocalDateTime startDate,
                                    LocalDate endDate,
                                    UUID productId){
        this.minQuantity = mintQuantity;
        this.maxQuantity = maxQuantity;
        this.title = title;
        this.description = description;
        this.discountedPrice = discountedPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sellerId = productId;
    }

}
