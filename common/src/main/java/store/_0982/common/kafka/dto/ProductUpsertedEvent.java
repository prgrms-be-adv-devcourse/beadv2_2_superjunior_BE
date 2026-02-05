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
public class ProductUpsertedEvent extends BaseEvent {

    private UUID productId;
    private String name;
    private String description;
    private Category category;

    public ProductUpsertedEvent(Clock clock, UUID productId, String name, String description, Category category) {
        super(clock);
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public enum Category {
        HOME,        // 생활 & 주방
        FOOD ,       // 식품 & 간식
        HEALTH,      // 건강 & 헬스
        BEAUTY,      // 뷰티
        FASHION,     // 패션 & 의류
        ELECTRONICS, // 전자 & 디지털
        KIDS,        // 유아 & 어린이
        HOBBY,       // 취미
        PET          // 반려동물
    }
}
