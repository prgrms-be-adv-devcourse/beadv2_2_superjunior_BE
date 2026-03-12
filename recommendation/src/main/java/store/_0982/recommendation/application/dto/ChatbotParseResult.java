package store._0982.recommendation.application.dto;

import java.util.List;

public record ChatbotParseResult(
        String intent,
        String category,
        String priceRange,
        List<String> preferences
) {
}
