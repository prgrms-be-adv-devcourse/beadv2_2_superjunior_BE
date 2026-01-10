package store._0982.commerce.domain.grouppurchase;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.kafka.dto.GroupPurchaseChangedEvent;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.kafka.dto.ProductEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "\"group_purchase\"", schema = "product_schema")
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
    private Long discountedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private GroupPurchaseStatus status;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "current_quantity", nullable = false)
    private int currentQuantity = 0;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(name = "settled_at")
    private OffsetDateTime settledAt;

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;
    
    public GroupPurchase(int mintQuantity,
                         int maxQuantity,
                         String title,
                         String description,
                         Long discountedPrice,
                         OffsetDateTime startDate,
                         OffsetDateTime endDate,
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

    public void syncCurrentQuantity(int count){
        this.currentQuantity = count;
        checkAndUpdateStatusIfMaxReached();
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
                                    Long discountedPrice,
                                    OffsetDateTime startDate,
                                    OffsetDateTime endDate,
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

    public void markAsSettled() {
        this.settledAt = OffsetDateTime.now();
    }

    public boolean isSettled() {
        return this.settledAt != null;
    }
  
    public void updateStatus(GroupPurchaseStatus status){
        this.status = status;
    }

    public void markAsReturned() {
        if (this.returnedAt != null) {
            throw new IllegalStateException("이미 환불 처리된 공동구매입니다.");
        }
        this.returnedAt = OffsetDateTime.now();
    }

    public boolean isReturned() {
        return this.returnedAt != null;
    }

    public GroupPurchaseEvent toEvent(String sellerName, GroupPurchaseEvent.SearchKafkaStatus searchKafkaStatus, ProductEvent productEvent) {
        return new GroupPurchaseEvent(
                this.groupPurchaseId,
                this.minQuantity,
                this.maxQuantity,
                this.title,
                this.description,
                this.discountedPrice,
                this.status.name(),
                sellerName,
                this.startDate.toString(),
                this.endDate.toString(),
                this.createdAt.toString(),
                this.updatedAt.toString(),
                this.currentQuantity,
                productEvent,
                searchKafkaStatus
        );
    }

    public GroupPurchaseChangedEvent toChangedEvent(GroupPurchaseChangedEvent.Status status, long totalAmount) {
        return new GroupPurchaseChangedEvent(
                this.groupPurchaseId,
                this.sellerId,
                this.title,
                status,
                totalAmount
        );
    }
}
