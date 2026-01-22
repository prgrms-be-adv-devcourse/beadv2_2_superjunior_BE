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
    private Status status;
    private String groupPurchaseName;       // 메인 공동구매 이름
    private int count;                      // 1부터 시작

    public SellerBalanceChangedEvent(Clock clock, UUID sellerId, long amount, Status status,
                                     String groupPurchaseName, int count) {
        super(clock);
        this.sellerId = sellerId;
        this.amount = amount;
        this.status = status;
        this.groupPurchaseName = groupPurchaseName;
        this.count = count;
    }

    public enum Status {
        CREDIT,     // 증가
        DEBIT       // 감소
    }
}
