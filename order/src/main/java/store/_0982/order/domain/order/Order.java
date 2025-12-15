package store._0982.order.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "\"order\"")
public class Order {
    @Id
    private UUID orderId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", nullable = false)
    private Long  price;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "address", nullable = false, length = 100)
    private String address;

    @Column(name = "address_detail", nullable = false, length = 100)
    private String addressDetail;

    @Column(name = "postal_code", nullable = false, length = 50)
    private String postalCode;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "group_purchase_id", nullable = false)
    private UUID groupPurchaseId;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;

    public Order(
            int quantity,
            Long price,
            UUID memberId,
            String address,
            String addressDetail,
            String postalCode,
            String receiverName,
            UUID sellerId,
            UUID groupPurchaseId) {
        this.orderId = UUID.randomUUID();
        this.quantity = quantity;
        this.price = price;
        this.memberId = memberId;
        this.status = OrderStatus.IN_PROGRESS;
        this.address = address;
        this.addressDetail = addressDetail;
        this.postalCode = postalCode;
        this.receiverName = receiverName;
        this.sellerId = sellerId;
        this.groupPurchaseId = groupPurchaseId;

    }

    // 상태 변경
    public void updateStatus(OrderStatus newStatus){
        validateStatus(this.status, newStatus );
        this.status = newStatus;
    }

    // 상태 전환 검증
    private void validateStatus(OrderStatus current, OrderStatus target) {
        if (current == target) {
            return;
        }

        switch (current) {
            case IN_PROGRESS -> {
                if (target != OrderStatus.SUCCESS && target != OrderStatus.FAILED) {
                    throw new IllegalStateException("SUCCESS, FAILED로만 변경 가능");
                }
            }
            case SUCCESS, FAILED ->
                throw new IllegalStateException("현재 상태 " + current + " 변경 불가능");

        }
    }

    // 환불 완료
    public void markReturned(){
        if(this.returnedAt != null){
            throw new IllegalStateException("이미 환불된 건입니다.");
        }
        this.returnedAt = OffsetDateTime.now();
    }

    // 환불 여부
    public boolean isReturned(){
        return this.returnedAt != null;
    }

}
