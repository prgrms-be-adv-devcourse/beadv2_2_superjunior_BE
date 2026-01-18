package store._0982.batch.application.settlement.event;

import store._0982.batch.domain.settlement.Settlement;

public record SettlementCompletedEvent(
        Settlement settlement
) {
}
