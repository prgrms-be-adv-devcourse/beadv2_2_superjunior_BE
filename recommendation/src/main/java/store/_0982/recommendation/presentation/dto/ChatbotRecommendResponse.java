package store._0982.recommendation.presentation.dto;

import java.util.List;

public record ChatbotRecommendResponse(
        ChatbotCriteria criteria,
        List<ChatbotRecommendationCard> results,
        List<String> suggestions,
        String assistantMessage,
        String nextQuestion
) {
}
