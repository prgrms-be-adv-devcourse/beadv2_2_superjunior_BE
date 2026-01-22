package store._0982.batch.domain.ai;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class CartVector extends ProductVector{
    private UUID cartId;
    int quantity;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;

    public CartVector(UUID memberId, UUID productId, float[] vector, int quantity, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        super(memberId, productId, vector);
        this.cartId = UUID.randomUUID();
        this.quantity = quantity;
        this.createdAt = createdAt;
    }
}
