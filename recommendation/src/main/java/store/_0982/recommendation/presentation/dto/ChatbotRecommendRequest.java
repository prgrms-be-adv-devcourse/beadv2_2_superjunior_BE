package store._0982.recommendation.presentation.dto;

public record ChatbotRecommendRequest(
        String message,
        String category,
        String priceRange,
        String preference
) {
}
