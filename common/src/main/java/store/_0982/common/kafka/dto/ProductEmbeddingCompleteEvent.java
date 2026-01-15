package store._0982.common.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEmbeddingCompleteEvent extends BaseEvent {
    private UUID productId;
    private List<Float> vector;

    public ProductEmbeddingCompleteEvent(Clock clock, UUID productId, List<Float> vector) {
        super(clock);
        this.productId = productId;
        this.vector = vector;
    }
}
