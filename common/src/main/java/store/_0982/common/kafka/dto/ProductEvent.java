package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S107"})
public class ProductEvent extends BaseEvent {
    private UUID id;
    private String name;
    private Long price;
    private String category;
    private String description;
    private Integer stock;
    private String originalUrl;
    private UUID sellerId;
    private String createdAt;
    private String updatedAt;

    public ProductEvent(Clock clock, UUID id, String name, long price, String category, String description, Integer stock, String originalUrl, UUID sellerId, String createdAt, String updatedAt) {
        super(clock);
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
