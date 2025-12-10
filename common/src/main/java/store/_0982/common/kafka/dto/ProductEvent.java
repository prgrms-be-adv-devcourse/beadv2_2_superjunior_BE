package store._0982.common.kafka.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class ProductEvent extends BaseEvent {
    private final UUID id;
    private final String name;
    private final long price;
    private final String category;
    private final String description;
    private final String originalUrl;
    private final UUID sellerId;

    public ProductEvent(Clock clock, UUID id, String name, long price, String category,
                        String description, String originalUrl, UUID sellerId) {
        super(clock);
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.originalUrl = originalUrl;
        this.sellerId = sellerId;
    }
}
