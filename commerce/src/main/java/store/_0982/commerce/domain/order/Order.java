package store._0982.commerce.domain.order;

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
@Table(name = "\"order\"", schema = "order_schema")
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

    private Order(
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
        this.status = OrderStatus.PENDING;
        this.address = address;
        this.addressDetail = addressDetail;
        this.postalCode = postalCode;
        this.receiverName = receiverName;
        this.sellerId = sellerId;
        this.groupPurchaseId = groupPurchaseId;
    }

    public static Order pending(
            int quantity,
            Long price,
            UUID memberId,
            String address,
            String addressDetail,
            String postalCode,
            String receiverName,
            UUID sellerId,
            UUID groupPurchaseId) {
        return new Order(
                quantity,
                price,
                memberId,
                address,
                addressDetail,
                postalCode,
                receiverName,
                sellerId,
                groupPurchaseId
        );
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

    // 상태 전이
    public void confirm(){
        if(this.status != OrderStatus.PENDING){
            throw new IllegalStateException("Order 상태 CONFIRMED 불가 status : " + status);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void markSuccess() {
        if(this.status != OrderStatus.CONFIRMED){
            throw new IllegalStateException("CONFIRMED 상태에서 SUCCESS로 변경 가능");
        }
        this.status = OrderStatus.SUCCESS;
    }

    public void markFailed() {
        if(this.status != OrderStatus.CONFIRMED){
            throw new IllegalStateException("CONFIRMED 상태에서 FAILED로 변경 가능");
        }
        this.status = OrderStatus.FAILED;
    }

    public void cancel() {
        if(this.status != OrderStatus.PENDING && this.status != OrderStatus.CONFIRMED){
            throw new IllegalStateException("PENDING과 CONFIRMED에서만 CANCEL 가능");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // 환불 여부
    public boolean isReturned(){
        return this.returnedAt != null;
    }

    // 환불 완료
    public void markReturned(){
        if(this.isReturned()){
            throw new IllegalStateException("이미 환불된 건입니다.");
        }
        this.returnedAt = OffsetDateTime.now();
    }



}
