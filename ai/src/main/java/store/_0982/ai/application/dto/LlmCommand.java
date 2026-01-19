package store._0982.ai.application.dto;

public record LlmCommand(
        String groupPurchaseId,
        String title,
        String description,
        String productId,
        String category
) {
    public static LlmCommand from(RecommandationSearchResponse recommandationSearchResponse) {
        return new LlmCommand(
                recommandationSearchResponse.groupPurchaseId(),
                recommandationSearchResponse.title(),
                recommandationSearchResponse.description(),
                recommandationSearchResponse.productSearchInfo().productId(),
                recommandationSearchResponse.productSearchInfo().category()
        );
    }
}
