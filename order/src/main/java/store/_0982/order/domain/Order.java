package store._0982.order.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.exception.CustomException;
import store._0982.order.application.dto.OrderRegisterCommand;
import store._0982.order.exception.CustomErrorCode;

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
    private int  price;

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
            int price,
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

    public static Order create(OrderRegisterCommand command, UUID memberId){
        return new Order(
                command.quantity(),
                command.price(),
                memberId,
                command.address(),
                command.addressDetail(),
                command.postalCode(),
                command.receiverName(),
                command.sellerId(),
                command.groupPurchaseId()
        );
    }

    // 취소 가능 여부
    public boolean isCancellable(){
        return this.status == OrderStatus.SCHEDULED
                && this.deletedAt == null;
    }

    // 주문 취소
    public void cancel() {
        if(!isCancellable()){
            throw new CustomException(CustomErrorCode.ORDER_NOT_CANCELLABLE);
        }
        this.deletedAt = OffsetDateTime.now();
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
            case SCHEDULED -> {
                if (target != OrderStatus.IN_PROGRESS && target != OrderStatus.FAILED) {
                    throw new IllegalStateException(
                            "SCHEDULED는 IN_PROGRESS 또는 FAILED로만 변경 가능합니다."
                    );
                }
            }
            case IN_PROGRESS -> {
                if (target != OrderStatus.SUCCESS && target != OrderStatus.FAILED) {
                    throw new IllegalStateException(
                            "IN_PROGRESS는 SUCCESS 또는 FAILED로만 변경 가능합니다."
                    );
                }
            }
            case SUCCESS, FAILED -> {
                throw new IllegalStateException(
                        "최종 상태(" + current + ")에서는 상태 변경이 불가능합니다."
                );
            }
        }
    }

    // 삭제 여부 확인
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    // 환불 완료
    public void markReturned(){
        if(this.returnedAt != null){
            throw new CustomException(CustomErrorCode.ALREADY_RETURNED);
        }
        this.returnedAt = OffsetDateTime.now();
    }

    // 환불 여부
    public boolean isReturned(){
        return this.returnedAt != null;
    }
}
