package store._0982.batch.domain.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public abstract class ProductVector {
    private UUID memberId;
    private UUID productId;
    float[] vector;
}
