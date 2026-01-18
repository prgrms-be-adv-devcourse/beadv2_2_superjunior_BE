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
public class ProductEmbeddingCompletedEvent extends BaseEvent {

    private UUID productId;
    float[] vector;

    public ProductEmbeddingCompletedEvent(Clock clock, UUID productId, float[] vector) {
        super(clock);
        this.productId = productId;
        this.vector = vector;
    }
}
