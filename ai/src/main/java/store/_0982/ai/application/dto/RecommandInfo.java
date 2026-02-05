package store._0982.ai.application.dto;

import java.util.List;

public record RecommandInfo(
    List<GroupPurchase> groupPurchase,
    String reason
){
}
