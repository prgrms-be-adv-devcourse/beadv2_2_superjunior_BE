package store._0982.common.kafka.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.UUID;

@Getter
public class ProductEvent extends BaseEvent {

    private final UUID id;
    private final String name;
    private final long price;
    private final String category;
    private final String description;
    private final Integer stock;
    private final String originalUrl;
    private final UUID sellerId;
    private final String createdAt;
    private final String updatedAt;

    @JsonCreator
    public ProductEvent(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name,
            @JsonProperty("price") long price,
            @JsonProperty("category") String category,
            @JsonProperty("description") String description,
            @JsonProperty("stock") Integer stock,
            @JsonProperty("originalUrl") String originalUrl,
            @JsonProperty("sellerId") UUID sellerId,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("updatedAt") String updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.stock = stock;
        this.originalUrl = originalUrl;
        this.sellerId = sellerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
