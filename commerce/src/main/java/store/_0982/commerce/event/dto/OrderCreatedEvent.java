package store._0982.commerce.event.dto;


import java.util.UUID;

public record OrderCreatedEvent(
        UUID memberId,
        UUID idempotencyKey,
        UUID orderId,
        Long amount
) {

}
