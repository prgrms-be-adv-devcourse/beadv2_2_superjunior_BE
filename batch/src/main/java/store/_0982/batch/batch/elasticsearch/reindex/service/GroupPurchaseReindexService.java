package store._0982.batch.batch.elasticsearch.reindex.service;

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
import store._0982.batch.batch.elasticsearch.reindex.domain.GroupPurchaseReindexRepository;
import store._0982.batch.batch.elasticsearch.reindex.domain.GroupPurchaseReindexRow;
import store._0982.batch.batch.elasticsearch.reindex.document.GroupPurchaseDocument;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPurchaseReindexService {

    private static final int RETRY_MAX_ATTEMPTS = 1;

    private final ElasticsearchOperations operations;
    private final ElasticsearchClient elasticsearchClient;
    private final GroupPurchaseReindexRepository reindexRepository;

    public void createIndexWithMapping(String indexName) {
        IndexOperations indexOps = operations.indexOps(IndexCoordinates.of(indexName));
        Settings settings = indexOps.createSettings(GroupPurchaseDocument.class);
        Document mapping = indexOps.createMapping(GroupPurchaseDocument.class);
        indexOps.create(settings, mapping);
    }

    public List<String> bulkIndex(String indexName, List<? extends GroupPurchaseDocument> docs) {
        BulkResponse response = null;
        try {
            response = elasticsearchClient.bulk(bulk -> {
                for (GroupPurchaseDocument doc : docs) {
                    bulk.operations(op -> op
                            .index(idx -> idx
                                    .index(indexName)
                                    .id(doc.getGroupPurchaseId())
                                    .document(doc)
                            ));
                }
                return bulk;
            });
        } catch (Exception e) {
            throw new CustomException(CustomErrorCode.ES_BULK_INSERT_ERROR);
        }
        return collectFailedIds(response);
    }

    public List<String> retryFailedRows(String indexName, List<String> failedIds) {
        if (failedIds == null || failedIds.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = failedIds.stream()
                .map(UUID::fromString)
                .toList();
        List<GroupPurchaseReindexRow> retryRows = reindexRepository.fetchByIds(ids);
        if (retryRows.isEmpty()) {
            return failedIds;
        }
        List<GroupPurchaseDocument> retryDocs = retryRows.stream()
                .map(GroupPurchaseDocument::fromReindexRow)
                .toList();

        List<String> retryFailed = failedIds;
        for (int attempt = 0; attempt < RETRY_MAX_ATTEMPTS; attempt++) {
            retryFailed = bulkIndex(indexName, retryDocs);
            if (retryFailed.isEmpty()) {
                break;
            }
        }
        return retryFailed;
    }

    public void switchAliasAfterValidation(String aliasName, String targetIndex) {
        long sourceCount = reindexRepository.countSource();
        long targetCount = countTargetIndex(targetIndex);
        if (sourceCount != targetCount) {
            throw new CustomException(CustomErrorCode.DB_ES_COUNT_MISMATCH);
        }
        switchAlias(aliasName, targetIndex);
    }

    private long countTargetIndex(String indexName) {
        operations.indexOps(IndexCoordinates.of(indexName)).refresh();
        return operations.count(Query.findAll(), GroupPurchaseDocument.class, IndexCoordinates.of(indexName));
    }

    private void switchAlias(String aliasName, String targetIndex) {
        IndexOperations aliasOps = operations.indexOps(IndexCoordinates.of(aliasName));
        Map<String, Set<org.springframework.data.elasticsearch.core.index.AliasData>> aliasMap =
                aliasOps.getAliases(aliasName);

        AliasActions actions = new AliasActions();
        for (String index : aliasMap.keySet()) {
            actions.add(new AliasAction.Remove(AliasActionParameters.builder()
                    .withIndices(index)
                    .withAliases(aliasName)
                    .build()));
        }
        actions.add(new AliasAction.Add(AliasActionParameters.builder()
                .withIndices(targetIndex)
                .withAliases(aliasName)
                .build()));

        operations.indexOps(IndexCoordinates.of(targetIndex)).alias(actions);
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
}
