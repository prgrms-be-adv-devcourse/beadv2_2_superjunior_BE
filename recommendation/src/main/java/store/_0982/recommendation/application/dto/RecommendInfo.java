package store._0982.recommendation.application.dto;

import java.util.List;

public record RecommendInfo(
    List<GroupPurchase> groupPurchase,
    String reason
){
}
