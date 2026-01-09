package store._0982.elasticsearch.application.reindex;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.AliasAction;
import org.springframework.data.elasticsearch.core.index.AliasActionParameters;
import org.springframework.data.elasticsearch.core.index.AliasActions;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.elasticsearch.application.dto.GroupPurchaseReindexInfo;
import store._0982.elasticsearch.domain.GroupPurchaseDocument;
import store._0982.elasticsearch.domain.reindex.GroupPurchaseReindexRepository;
import store._0982.elasticsearch.domain.reindex.GroupPurchaseReindexRow;
import store._0982.elasticsearch.exception.CustomErrorCode;
import store._0982.elasticsearch.reindex.GroupPurchaseReindexProperties;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GroupPurchaseReindexService {

    private static final String INDEX_SUFFIX_PATTERN = "yyyyMMddHHmmss";

    private final ElasticsearchOperations operations;
    private final ElasticsearchClient elasticsearchClient;
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
        return new GroupPurchaseReindexInfo(indexName, indexed, null, null, false);
    }

    @ServiceLog
    public GroupPurchaseReindexInfo reindexIncrementalAndMaybeSwitch(String indexName, OffsetDateTime since, boolean autoSwitch) {
        if (indexName == null || indexName.isBlank()) {
            throw new CustomException(CustomErrorCode.INDEX_NAME_ISNULL);
        }
        if (!autoSwitch) {
            return new GroupPurchaseReindexInfo(indexName, reindexIncrementalByIndex(indexName, since), null, null, false);
        }
        long indexed = reindexIncrementalByIndex(indexName, since);
        long sourceCount = reindexRepository.countSource();
        long targetCount = countTargetIndex(indexName);
        if (sourceCount != targetCount) {
            throw new CustomException(CustomErrorCode.REINDEX_COUNT_MISMATCH);
        }
        switchAlias(properties.getAlias(), indexName);
        return new GroupPurchaseReindexInfo(indexName, indexed, null, null, true);
    }

    @ServiceLog
    public GroupPurchaseReindexInfo reindexFullAndIncremental(boolean autoSwitch) {
        String indexName = createIndex();
        OffsetDateTime since = OffsetDateTime.now();
        long fullIndexed = reindexAll(indexName).indexed();
        long incrementalIndexed = reindexIncrementalByIndex(indexName, since);
        long sourceCount = reindexRepository.countSource();
        long targetCount = countTargetIndex(indexName);
        boolean switched = false;
        if (autoSwitch) {
            if (sourceCount != targetCount) {
                throw new CustomException(CustomErrorCode.REINDEX_COUNT_MISMATCH);
            }
            switchAlias(properties.getAlias(), indexName);
            switched = true;
        }
        return new GroupPurchaseReindexInfo(indexName, null, fullIndexed, incrementalIndexed, switched);
    }

    public long countTargetIndex(String indexName) {
        operations.indexOps(IndexCoordinates.of(indexName)).refresh();
        return operations.count(Query.findAll(), GroupPurchaseDocument.class, IndexCoordinates.of(indexName));
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
            List<String> failedIds = bulkIndex(indexName, rows);
            //실패 부분 재시도 처리 필요
            total += rows.size() - failedIds.size();
            offset += batchSize;
        }
        return total;
    }

    private String createIndexWithMapping(String aliasName) {
        String newIndex = aliasName + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(INDEX_SUFFIX_PATTERN));
        IndexOperations indexOps = operations.indexOps(IndexCoordinates.of(newIndex));

        Settings settings = indexOps.createSettings(GroupPurchaseDocument.class);
        Document mapping = indexOps.createMapping(GroupPurchaseDocument.class);
        indexOps.create(settings, mapping);
        return newIndex;
    }

    //bulk 부분 실패 판단을 위해 저수준 elc로 처리
    private List<String> bulkIndex(String indexName, List<GroupPurchaseReindexRow> rows) {
        try {
            BulkResponse response = elasticsearchClient.bulk(bulk -> {
                for (GroupPurchaseReindexRow row : rows) {
                    bulk.operations(op -> op
                            .index(idx -> idx
                                    .index(indexName)
                                    .id(row.groupPurchaseId().toString())
                                    .document(GroupPurchaseDocument.fromReindexRow(row))
                            ));
                }
                return bulk;
            });
            return collectFailedIds(response);
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> collectFailedIds(BulkResponse response) {
        if (!response.errors()) {
            return List.of();
        }
        List<String> failedIds = new ArrayList<>();
        response.items().forEach(item -> {
            if (item.error() != null && item.id() != null) {
                failedIds.add(item.id());
            }
        });
        return failedIds;
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

    //전체 재색인과 증분 재색인은 입력값만 다를 뿐 동일한 배치 로직을 수행하므로 추상화
    @FunctionalInterface
    private interface RowFetcher {
        List<GroupPurchaseReindexRow> fetch(int limit, long offset);
    }

}
