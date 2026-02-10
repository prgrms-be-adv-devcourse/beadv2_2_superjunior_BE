package store._0982.recommendation.application.dto;

import java.util.List;

public record RecommandInfo(
    List<GroupPurchase> groupPurchase,
    String reason
){
}
