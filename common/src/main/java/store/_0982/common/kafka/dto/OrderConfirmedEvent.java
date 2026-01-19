package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("unused")
public class OrderConfirmedEvent extends BaseEvent {

    private UUID orderId;
    private UUID memberId;
    private UUID groupPurchaseId;
    private String productName;
    private ProductCategory productCategory;

    public OrderConfirmedEvent(Clock clock, UUID orderId, UUID memberId, UUID groupPurchaseId,
                               String productName, ProductCategory productCategory) {
        super(clock);
        this.orderId = orderId;
        this.memberId = memberId;
        this.groupPurchaseId = groupPurchaseId;
        this.productName = productName;
        this.productCategory = productCategory;
    }

    public enum ProductCategory {
        HOME,        // 생활 & 주방
        FOOD,        // 식품 & 간식
        HEALTH,      // 건강 & 헬스
        BEAUTY,      // 뷰티
        FASHION,     // 패션 & 의류
        ELECTRONICS, // 전자 & 디지털
        KIDS,        // 유아 & 어린이
        HOBBY,       // 취미
        PET          // 반려동물
    }
}
