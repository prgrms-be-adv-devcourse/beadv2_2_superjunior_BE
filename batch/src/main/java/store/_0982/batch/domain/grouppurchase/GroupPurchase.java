package store._0982.batch.domain.grouppurchase;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

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

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;

    @Column(name = "succeeded_at")
    private OffsetDateTime succeededAt;

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

    public void open() {
        if(this.status != GroupPurchaseStatus.SCHEDULED){
            throw new IllegalStateException("SCHEDULED 일 때만 변경 가능");
        }
        this.status = GroupPurchaseStatus.OPEN;
    }

    public void markSuccess(){
        if(this.status != GroupPurchaseStatus.OPEN){
            throw new IllegalStateException("OPEN 일 때만 변경 가능");
        }
        this.status = GroupPurchaseStatus.SUCCESS;
    }

    public void markFailed(){
        if(this.status != GroupPurchaseStatus.OPEN){
            throw new IllegalStateException("OPEN 일 때만 변경 가능");
        }
        this.status = GroupPurchaseStatus.FAILED;
    }

    public GroupPurchaseEvent toEvent(GroupPurchaseEvent.Status groupPurchaseStatus,
                                      GroupPurchaseEvent.EventStatus kafkaStatus,
                                      Long originalPrice,
                                      GroupPurchaseEvent.ProductCategory category) {
        return new GroupPurchaseEvent(
                this.groupPurchaseId,
                this.sellerId,
                this.title,
                this.description,
                this.discountedPrice,
                this.productId,
                groupPurchaseStatus,
                this.endDate.toString(),
                this.updatedAt.toString(),
                this.currentQuantity,
                kafkaStatus,
                originalPrice,
                category
        );
    }
}
