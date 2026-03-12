package store._0982.recommendation.application.dto;

import java.util.List;

public record ChatbotParseArgs(
        String intent,
        String category,
        String priceRange,
        List<String> preferences
) {
}
