package store._0982.recommendation.presentation.dto;

public record ChatbotRequest(
        String message,
        String category,
        String priceRange,
        String preferences
) {
}
