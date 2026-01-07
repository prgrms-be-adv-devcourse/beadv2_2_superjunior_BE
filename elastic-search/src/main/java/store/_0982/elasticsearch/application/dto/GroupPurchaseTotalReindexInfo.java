package store._0982.elasticsearch.application.dto;

public record GroupPurchaseTotalReindexInfo(
        String indexName,
        Long fullIndexed,
        Long incrementalIndexed,
        boolean switched
) {
    public static GroupPurchaseTotalReindexInfo from(GroupPurchaseReindexSummary summary) {
        return new GroupPurchaseTotalReindexInfo(
                summary.indexName(),
                summary.fullIndexed(),
                summary.incrementalIndexed(),
                summary.switched()
        );
    }
}
