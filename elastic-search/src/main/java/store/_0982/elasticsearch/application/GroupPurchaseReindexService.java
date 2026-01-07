package store._0982.elasticsearch.application;

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
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseReindexInfo;
import store._0982.elasticsearch.application.dto.GroupPurchaseTotalReindexInfo;
import store._0982.elasticsearch.application.dto.GroupPurchaseReindexSummary;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.ProductDocumentEmbedded;
import store._0982.elasticsearch.exception.CustomErrorCode;
import store._0982.elasticsearch.infrastructure.reindex.GroupPurchaseReindexRepository;
import store._0982.elasticsearch.infrastructure.reindex.GroupPurchaseReindexRow;
import store._0982.elasticsearch.reindex.GroupPurchaseReindexProperties;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
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

    private final ElasticsearchOperations operations;
    private final GroupPurchaseReindexProperties properties;
    private final GroupPurchaseReindexRepository reindexRepository;

    @ServiceLog
    public void reindex() {
        String aliasName = properties.getAlias();
        String newIndex = createIndexWithMapping(aliasName);

        reindexAll(newIndex);

        if (properties.isSwitchAlias()) {
            switchAlias(aliasName, newIndex);
        }
    }

    @ServiceLog
    public String createIndex() {
        return createIndexWithMapping(properties.getAlias());
    }

    @ServiceLog
    public GroupPurchaseReindexInfo reindexAll(String targetIndex) {
        String indexName;
        if (targetIndex == null || targetIndex.isBlank()) {
            indexName = createIndex();
        }
        else{
            indexName = targetIndex;
        }
        long indexed = reindexAllByIndex(indexName);
        return new GroupPurchaseReindexInfo(indexName, indexed);
    }

    public long reindexIncremental(String indexName, OffsetDateTime since) {
        return reindexIncrementalByIndex(indexName, since);
    }

    @ServiceLog
    public GroupPurchaseReindexInfo reindexIncrementalAndMaybeSwitch(String indexName, OffsetDateTime since, boolean autoSwitch) {
        if (!autoSwitch) {
            return new GroupPurchaseReindexInfo(indexName, reindexIncremental(indexName, since));
        }
        long indexed = reindexIncremental(indexName, since);
        long sourceCount = countSource();
        long targetCount = countTargetIndex(indexName);
        if (sourceCount != targetCount) {
            throw new CustomException(CustomErrorCode.REINDEX_COUNT_MISMATCH);
        }
        switchAlias(properties.getAlias(), indexName);
        return new GroupPurchaseReindexInfo(indexName, indexed);
    }

    @ServiceLog
    public GroupPurchaseTotalReindexInfo reindexFullAndIncremental(boolean autoSwitch) {
        String indexName = createIndex();
        OffsetDateTime since = OffsetDateTime.now();
        long fullIndexed = reindexAll(indexName).indexed();
        long incrementalIndexed = reindexIncremental(indexName, since);
        long sourceCount = countSource();
        long targetCount = countTargetIndex(indexName);
        boolean switched = false;
        if (autoSwitch) {
            if (sourceCount != targetCount) {
                throw new CustomException(CustomErrorCode.REINDEX_COUNT_MISMATCH);
            }
            switchAlias(properties.getAlias(), indexName);
            switched = true;
        }
        GroupPurchaseReindexSummary summary = new GroupPurchaseReindexSummary(indexName, fullIndexed, incrementalIndexed, switched);
        return GroupPurchaseTotalReindexInfo.from(summary);
    }

    public long countTargetIndex(String indexName) {
        operations.indexOps(IndexCoordinates.of(indexName)).refresh();
        return operations.count(Query.findAll(), GroupPurchaseDocument.class, IndexCoordinates.of(indexName));
    }

    public long countSource() {
        return reindexRepository.countSource();
    }

    private long reindexAllByIndex(String indexName) {
        return reindexByFetcher(indexName, reindexRepository::fetchAllRows);
    }

    private long reindexIncrementalByIndex(String indexName, OffsetDateTime since) {
        return reindexByFetcher(indexName, (limit, offset) -> reindexRepository.fetchIncrementalRows(since, limit, offset));
    }

    private long reindexByFetcher(String indexName, RowFetcher fetcher) {
        int batchSize = properties.getBatchSize();
        long offset = 0;
        long total = 0;

        while (true) {
            List<GroupPurchaseReindexRow> rows = fetcher.fetch(batchSize, offset);
            if (rows.isEmpty()) {
                break;
            }
            bulkIndex(indexName, rows);
            total += rows.size();
            offset += batchSize;
            log.debug("Reindex progress: indexed={}", total);
        }
        return total;
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
        OffsetDateTime startDate = toOffsetDateTime(row.startDate());
        OffsetDateTime endDate = toOffsetDateTime(row.endDate());
        return GroupPurchaseDocument.builder()
                .groupPurchaseId(row.groupPurchaseId().toString())
                .sellerName(null)
                .minQuantity(row.minQuantity())
                .maxQuantity(row.maxQuantity())
                .title(row.title())
                .description(row.description())
                .discountedPrice(row.discountedPrice())
                .status(row.status())
                .startDate(startDate != null ? startDate.toString() : null)
                .endDate(endDate != null ? endDate.toString() : null)
                .createdAt(toOffsetDateTime(row.createdAt()))
                .updatedAt(toOffsetDateTime(row.updatedAt()))
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

    private OffsetDateTime toOffsetDateTime(java.time.Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
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

    @FunctionalInterface
    private interface RowFetcher {
        List<GroupPurchaseReindexRow> fetch(int limit, long offset);
    }

}
