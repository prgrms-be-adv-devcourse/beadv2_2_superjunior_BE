package store._0982.elasticsearch.reindex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.ProductDocumentEmbedded;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupPurchaseReindexService {

    private static final String INDEX_SUFFIX_PATTERN = "yyyyMMddHHmmss";

    private static final String REINDEX_SQL = """
            select
                gp.group_purchase_id,
                gp.title,
                gp.description,
                gp.status,
                gp.start_date,
                gp.end_date,
                gp.min_quantity,
                gp.max_quantity,
                gp.discounted_price,
                gp.current_quantity,
                gp.created_at,
                gp.updated_at,
                p.product_id,
                p.category,
                p.price,
                p.original_url,
                p.seller_id
            from product_schema.group_purchase gp
            join product_schema.product p on p.product_id = gp.product_id
            order by gp.group_purchase_id
            limit ? offset ?
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ElasticsearchOperations operations;
    private final GroupPurchaseReindexProperties properties;

    public void reindex() {
        String aliasName = properties.getAlias();
        String newIndex = createIndexWithMapping(aliasName);
        log.info("Start GroupPurchase reindex to index={}", newIndex);

        int batchSize = properties.getBatchSize();
        long offset = 0;
        long total = 0;

        while (true) {
            List<GroupPurchaseReindexRow> rows = fetchRows(batchSize, offset);
            if (rows.isEmpty()) {
                break;
            }
            bulkIndex(newIndex, rows);
            total += rows.size();
            offset += batchSize;
            log.info("Reindex progress: indexed={}", total);
        }

        if (properties.isSwitchAlias()) {
            switchAlias(aliasName, newIndex);
        }
        log.info("GroupPurchase reindex completed. indexed={}", total);
    }

    private List<GroupPurchaseReindexRow> fetchRows(int batchSize, long offset) {
        return jdbcTemplate.query(
                REINDEX_SQL,
                (rs, rowNum) -> new GroupPurchaseReindexRow(
                        rs.getObject("group_purchase_id", UUID.class),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getObject("start_date", OffsetDateTime.class),
                        rs.getObject("end_date", OffsetDateTime.class),
                        rs.getInt("min_quantity"),
                        rs.getInt("max_quantity"),
                        rs.getLong("discounted_price"),
                        rs.getInt("current_quantity"),
                        rs.getObject("created_at", OffsetDateTime.class),
                        rs.getObject("updated_at", OffsetDateTime.class),
                        rs.getObject("product_id", UUID.class),
                        rs.getString("category"),
                        rs.getLong("price"),
                        rs.getString("original_url"),
                        rs.getObject("seller_id", UUID.class)
                ),
                batchSize,
                offset
        );
    }

    private String createIndexWithMapping(String aliasName) {
        String newIndex = aliasName + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(INDEX_SUFFIX_PATTERN));
        IndexOperations indexOps = operations.indexOps(IndexCoordinates.of(newIndex));

        if (indexOps.exists()) {
            throw new IllegalStateException("index already exists: " + newIndex);
        }

        Settings settings = indexOps.createSettings(GroupPurchaseDocument.class);
        Document mapping = indexOps.createMapping(GroupPurchaseDocument.class);
        indexOps.create(settings, mapping);
        return newIndex;
    }

    private void bulkIndex(String indexName, List<GroupPurchaseReindexRow> rows) {
        List<IndexQuery> queries = rows.stream()
                .map(row -> new IndexQueryBuilder()
                        .withId(row.groupPurchaseId().toString())
                        .withObject(toDocument(row))
                        .build())
                .toList();
        operations.bulkIndex(queries, IndexCoordinates.of(indexName));
    }

    private GroupPurchaseDocument toDocument(GroupPurchaseReindexRow row) {
        return GroupPurchaseDocument.builder()
                .groupPurchaseId(row.groupPurchaseId().toString())
                .sellerName(null)
                .minQuantity(row.minQuantity())
                .maxQuantity(row.maxQuantity())
                .title(row.title())
                .description(row.description())
                .discountedPrice(row.discountedPrice())
                .status(row.status())
                .startDate(row.startDate() != null ? row.startDate().toString() : null)
                .endDate(row.endDate() != null ? row.endDate().toString() : null)
                .createdAt(row.createdAt())
                .updatedAt(row.updatedAt())
                .currentQuantity(row.currentQuantity())
                .discountRate(calculateDiscountRate(row.price(), row.discountedPrice()))
                .productDocumentEmbedded(new ProductDocumentEmbedded(
                        row.productId().toString(),
                        row.category(),
                        row.price(),
                        row.originalUrl(),
                        row.sellerId().toString()
                ))
                .build();
    }

    private long calculateDiscountRate(Long price, Long discountedPrice) {
        if (price == null || discountedPrice == null) {
            return 0L;
        }
        if (price <= 0 || discountedPrice >= price) {
            return 0L;
        }
        return Math.round(((double) (price - discountedPrice) / price) * 100);
    }

    private void switchAlias(String aliasName, String newIndex) {
        IndexOperations aliasOps = operations.indexOps(IndexCoordinates.of(aliasName));
        Map<String, Set<org.springframework.data.elasticsearch.core.index.AliasData>> aliasMap = aliasOps.getAliases(aliasName);

        AliasActions actions = new AliasActions();
        for (String index : aliasMap.keySet()) {
            actions.add(new AliasAction.Remove(AliasActionParameters.builder()
                    .withIndices(index)
                    .withAliases(aliasName)
                    .build()));
        }
        actions.add(new AliasAction.Add(AliasActionParameters.builder()
                .withIndices(newIndex)
                .withAliases(aliasName)
                .build()));

        operations.indexOps(IndexCoordinates.of(newIndex)).alias(actions);
    }

    private record GroupPurchaseReindexRow(
            UUID groupPurchaseId,
            String title,
            String description,
            String status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            int minQuantity,
            int maxQuantity,
            long discountedPrice,
            int currentQuantity,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            UUID productId,
            String category,
            Long price,
            String originalUrl,
            UUID sellerId
    ) {
    }
}
