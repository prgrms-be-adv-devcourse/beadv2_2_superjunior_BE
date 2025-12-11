package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings({"unused", "java:S107"})
public class ProductEvent extends BaseEvent {
    private UUID id;
    private String name;
    private long price;
    private String category;
    private String description;
    private Integer stock;
    private String originalUrl;
    private UUID sellerId;
    private String createdAt;
    private String updatedAt;
}
