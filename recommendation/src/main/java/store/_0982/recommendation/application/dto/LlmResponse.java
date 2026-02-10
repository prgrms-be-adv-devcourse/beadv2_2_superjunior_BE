package store._0982.recommendation.application.dto;

import java.util.List;
import java.util.UUID;

public record LlmResponse (
        List<GroupPurchase> groupPurchases,
        String reason
){
    public record GroupPurchase(
            UUID groupPurchaseId,
            Integer rank
    ){
    }
}
