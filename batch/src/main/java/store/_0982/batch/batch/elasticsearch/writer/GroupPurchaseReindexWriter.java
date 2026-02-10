package store._0982.batch.batch.elasticsearch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import store._0982.batch.domain.elasticsearch.GroupPurchaseDocument;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRow;
import store._0982.batch.application.elasticsearch.GroupPurchaseReindexService;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class GroupPurchaseReindexWriter implements ItemWriter<GroupPurchaseReindexRow> {

    private final GroupPurchaseReindexService reindexService;
    private final String targetIndex;

    @Override
    public void write(Chunk<? extends GroupPurchaseReindexRow> chunk) {
        List<? extends GroupPurchaseReindexRow> rows = chunk.getItems();
        if (rows.isEmpty()) {
            return;
        }
        List<GroupPurchaseDocument> docs = reindexService.buildDocumentsFromRows(rows);
        List<String> failedIds = reindexService.bulkIndex(targetIndex, docs);
        log.info("Reindex bulk completed: index={}, requested={}, indexed={}, failed={}",
                targetIndex,
                rows.size(),
                docs.size() - failedIds.size(),
                failedIds.size());
        if (!failedIds.isEmpty()) {
            List<String> retryFailed = reindexService.retryFailedRows(targetIndex, failedIds);
            if (!retryFailed.isEmpty()) {
                log.error("Reindex retry failed ids: {}", retryFailed);
                throw new CustomException(CustomErrorCode.ES_REINDEX_RETRY_FAILED);
            }
        }
    }
}
