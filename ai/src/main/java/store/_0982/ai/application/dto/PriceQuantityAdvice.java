package store._0982.ai.application.dto;

import java.util.List;

public record PriceQuantityAdvice(
        Long recommendedDiscountedPrice,
        int recommendedMinQuantity,
        int recommendedMaxQuantity,
        double recommendedDiscountRate,
        int recommendedDurationDays,
        String reason,
        double confidence,
        int analyzedCases,
        List<SimilarCaseSummary> similarCases
) {
}
