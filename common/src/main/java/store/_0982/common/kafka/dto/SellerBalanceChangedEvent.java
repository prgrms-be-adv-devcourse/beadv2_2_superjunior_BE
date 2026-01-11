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
public class SellerBalanceChangedEvent extends BaseEvent {

    private UUID sellerId;
    private long amount;
    private long totalBalance;
    private Status status;

    public SellerBalanceChangedEvent(Clock clock, UUID sellerId, long amount, long totalBalance, Status status) {
        super(clock);
        this.sellerId = sellerId;
        this.amount = amount;
        this.totalBalance = totalBalance;
        this.status = status;
    }

    public enum Status {
        CREDIT,     // 증가
        DEBIT       // 감소
    }
}
