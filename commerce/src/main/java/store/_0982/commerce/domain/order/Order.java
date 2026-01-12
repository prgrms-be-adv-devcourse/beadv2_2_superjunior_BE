package store._0982.commerce.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.OrderCanceledEvent;

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
    private Long price;

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

    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "expired_at")
    private OffsetDateTime expiredAt;

    @Column(name = "paid_at")
    private OffsetDateTime paidAt;

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
            UUID groupPurchaseId,
            String idempotencyKey) {
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
        this.idempotencyKey = idempotencyKey;
        this.expiredAt = OffsetDateTime.now().plusMinutes(10);
    }

    public static Order create(int quantity,
                               Long price,
                               UUID memberId,
                               String address,
                               String addressDetail,
                               String postalCode,
                               String receiverName,
                               UUID sellerId,
                               UUID groupPurchaseId,
                               String idempotencyKey) {
        return new Order(
                quantity,
                price,
                memberId,
                address,
                addressDetail,
                postalCode,
                receiverName,
                sellerId,
                groupPurchaseId,
                idempotencyKey
        );
    }

    // 상태 변경
    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    // 결제 완료
    public void completePayment(PaymentMethod paymentMethod) {
        validateStatus(OrderStatus.PAYMENT_COMPLETED);
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.paymentMethod = paymentMethod;
        this.paidAt = OffsetDateTime.now();
    }

    // 주문 실패 처리
    public void markFailed() {
        validateStatus(OrderStatus.ORDER_FAILED);
        this.status = OrderStatus.ORDER_FAILED;
    }

    // 주문 취소 처리
    public void cancel() {
        validateStatus(OrderStatus.CANCELLED);
        this.status = OrderStatus.CANCELLED;
    }

    public void requestCancel() {
        if (this.status != OrderStatus.PAYMENT_COMPLETED)
            throw new CustomException(CustomErrorCode.CANNOT_CANCEL_ORDER_INVALID_STATUS);
        this.status = OrderStatus.CANCEL_REQUESTED;
    }

    public void requestReversed() {
        if (this.status != OrderStatus.GROUP_PURCHASE_SUCCESS)
            throw new CustomException(CustomErrorCode.CANNOT_REVERSE_ORDER_INVALID_STATUS);
        this.status = OrderStatus.REVERSED_REQUESTED;
    }

    public void requestReturned() {
        if (this.status != OrderStatus.GROUP_PURCHASE_SUCCESS)
            throw new CustomException(CustomErrorCode.CANNOT_RETURN_ORDER_INVALID_STATUS);
        this.status = OrderStatus.REVERSED_REQUESTED;
    }

    private void validateStatus(OrderStatus newStatus) {
        switch (this.status) {
            case PENDING:
                if (newStatus != OrderStatus.PAYMENT_COMPLETED
                        && newStatus != OrderStatus.CANCELLED
                        && newStatus != OrderStatus.ORDER_FAILED) {
                    throw new IllegalStateException("PENDING으로 변경 불가능");
                }
                break;
            case PAYMENT_COMPLETED:
                if (newStatus != OrderStatus.CANCELLED
                        && newStatus != OrderStatus.RETURNED
                        && newStatus != OrderStatus.REVERSED) {
                    throw new IllegalStateException("PAYMENT_COMPLETED로 변경 불가능");
                }
                break;
            case ORDER_FAILED:
            case CANCELLED:
            case RETURNED:
                throw new IllegalStateException("상태 변경 불가능");

            default:
                break;
        }
    }

    // 환불 완료
    public void markReturned() {
        if (this.returnedAt != null) {
            throw new IllegalStateException("이미 환불된 건입니다.");
        }
        this.returnedAt = OffsetDateTime.now();
    }

    // 환불 여부
    public boolean isReturned() {
        return this.returnedAt != null;
    }

    public OrderCanceledEvent toEvent(String cancelReason, OrderCanceledEvent.PaymentMethod method, Long amount) {
        return new OrderCanceledEvent(
                this.memberId,
                this.orderId,
                cancelReason,
                method,
                amount
        );
    }
}
