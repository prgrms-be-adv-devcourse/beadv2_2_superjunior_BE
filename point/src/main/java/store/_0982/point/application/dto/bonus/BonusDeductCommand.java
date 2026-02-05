package store._0982.point.application.dto.bonus;

import java.util.UUID;

public record BonusDeductCommand(
        UUID transactionId,
        long deductAmount
) {
}
