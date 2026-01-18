package store._0982.batch.domain.ai;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public abstract class ProductVector {
    private UUID memberId;
    private UUID productId;
    float[] vector;
}
