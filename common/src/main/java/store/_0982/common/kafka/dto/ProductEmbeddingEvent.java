package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEmbeddingEvent extends BaseEvent{
    private UUID productId;
    private String name;
    private String description;
    private ProductCategory category;

    public ProductEmbeddingEvent(Clock clock, UUID productId, String name, String description, ProductCategory category) {
        super(clock);
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public enum ProductCategory {
        HOME,
        FOOD,
        HEALTH,
        BEAUTY,
        FASHION,
        ELECTRONICS,
        KIDS,
        HOBBY,
        PET
    }
}
