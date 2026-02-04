package store._0982.common.domain.order;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.exception.CustomException;
import store._0982.common.exception.EntityErrorCode;
import store._0982.common.kafka.dto.OrderCanceledEvent;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "\"order\"", schema = "order_schema")
public class Order {

    @Id
    @Column(name = "order_id", nullable = false, unique = true, updatable = false)
    private UUID orderId;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Column(name = "quantity", nullable = false, updatable = false)
    private int quantity;

    @Column(name = "price", nullable = false, updatable = false)
    private Long price;

    @Column(name = "paid_price", nullable = false, updatable = false)
    private Long paidPrice;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "member_id", nullable = false, updatable = false)
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
    private OffsetDateTime expiredAt; // 주문 만료 시간

    @Column(name = "paid_at")
    private OffsetDateTime paidAt; // 결제 완료 시간

    @Column(name = "canceled_at")
    private OffsetDateTime canceledAt; // 취소 완료 시간

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    private Order(
            int quantity,
            Long price,
            Long paidPrice,
            UUID memberId,
            String address,
            String addressDetail,
            String postalCode,
            String receiverName,
            UUID sellerId,
            UUID groupPurchaseId,
            String idempotencyKey) {
        this.orderId = UUID.randomUUID();
        this.orderNumber = generateOrderNumber();
        this.quantity = quantity;
        this.price = price;
        this.paidPrice = paidPrice;
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
                               Long paidPrice,
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
                paidPrice,
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

    public static String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String random = generateRandomAlphanumeric();
        return "ORD-" + date + "-" + random;
    }

    private static String generateRandomAlphanumeric() {
        String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom RANDOM = new SecureRandom();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    // 결제 완료
    public void completePayment(PaymentMethod paymentMethod) {
        // 이미 결제 완료된 건인지 확인
        if(this.status == OrderStatus.PAYMENT_COMPLETED){
            return;
        }

        if(this.status != OrderStatus.PENDING){
            throw new CustomException(EntityErrorCode.CANNOT_PAYMENT_COMPLETED_ORDER_INVALID_STATUS);
        }
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.paymentMethod = paymentMethod;
        this.paidAt = OffsetDateTime.now();
    }

    // 주문 취소 처리
    public void markFailed() {
        // 이미 결제 실패된 건인지 확인
        if(this.status == OrderStatus.PAYMENT_FAILED){
            return;
        }

        if(this.status != OrderStatus.PENDING){
            throw new CustomException(EntityErrorCode.CANNOT_PAYMENT_FAILED_ORDER_INVALID_STATUS);
        }
        this.status = OrderStatus.PAYMENT_FAILED;
    }

    public void markExpired(){
        if(this.status != OrderStatus.PENDING){
            return;
        }
        this.status = OrderStatus.EXPIRED;
    }
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(this.expiredAt);
    }

    public void confirmed(){
        if(this.status != OrderStatus.PAYMENT_COMPLETED){
            throw new CustomException(EntityErrorCode.CANNOT_PURCHASE_CONFIRM_ORDER_INVALID_STATUS);
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public OrderCanceledEvent toEvent(String productName, String cancelReason, OrderCanceledEvent.PaymentMethod method, Long amount) {
        return new OrderCanceledEvent(
                this.memberId,
                this.orderId,
                productName,
                cancelReason,
                method,
                amount
        );
    }
}
