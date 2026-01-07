package store._0982.elasticsearch.application.dto;

public record GroupPurchaseReindexSummary(
            String indexName,
            Long fullIndexed,
            Long incrementalIndexed,
            boolean switched
    ) {
    }
