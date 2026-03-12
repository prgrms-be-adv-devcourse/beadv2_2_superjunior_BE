package store._0982.recommendation.application.dto;

import java.util.List;

public record ChatbotResponse(
        String answer,
        List<GroupPurchase> recommendations,
        String nextQuestion
) {
}
