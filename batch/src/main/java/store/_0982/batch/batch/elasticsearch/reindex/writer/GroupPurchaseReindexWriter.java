package store._0982.batch.batch.elasticsearch.reindex.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import store._0982.batch.batch.elasticsearch.reindex.document.GroupPurchaseDocument;
import store._0982.batch.batch.elasticsearch.reindex.service.GroupPurchaseReindexService;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class GroupPurchaseReindexWriter implements ItemWriter<GroupPurchaseDocument> {

    private final GroupPurchaseReindexService reindexService;
    private final String targetIndex;

    @Override
    public void write(Chunk<? extends GroupPurchaseDocument> chunk) {
        List<? extends GroupPurchaseDocument> docs = chunk.getItems();
        if (docs.isEmpty()) {
            return;
        }
        List<String> failedIds = reindexService.bulkIndex(targetIndex, docs);
        if (!failedIds.isEmpty()) {
            List<String> retryFailed = reindexService.retryFailedRows(targetIndex, failedIds);
            if (!retryFailed.isEmpty()) {
                log.error("재색인 최종 실패 목록: {}", retryFailed);
            }
        }
    }
}
