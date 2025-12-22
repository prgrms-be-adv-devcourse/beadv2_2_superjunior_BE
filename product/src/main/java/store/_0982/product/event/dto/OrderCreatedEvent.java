package store._0982.product.event.dto;


import java.util.UUID;

public record OrderCreatedEvent(
        UUID memberId,
        UUID idempotencyKey,
        UUID orderId,
        Long amount
) {

}
