package store._0982.common.kafka.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class OrderEvent extends BaseEvent {
    private final UUID id;
    private final UUID memberId;
    private final int price;
    private final int quantity;
    private final String address;
    private final String addressDetail;
    private final String postalCode;
    private final String status;    // Order의 status

    public OrderEvent(Clock clock, UUID id, UUID memberId, int price, int quantity, String address,
                      String addressDetail, String postalCode, String status) {
        super(clock);
        this.id = id;
        this.memberId = memberId;
        this.price = price;
        this.quantity = quantity;
        this.address = address;
        this.addressDetail = addressDetail;
        this.postalCode = postalCode;
        this.status = status;
    }
}
