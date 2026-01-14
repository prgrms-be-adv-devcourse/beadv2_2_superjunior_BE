package store._0982.batch.batch.elasticsearch.reindex.reader;

import org.springframework.batch.item.ItemReader;
import store._0982.batch.batch.elasticsearch.reindex.domain.GroupPurchaseReindexRepository;
import store._0982.batch.batch.elasticsearch.reindex.domain.GroupPurchaseReindexRow;

import java.time.OffsetDateTime;
import java.util.List;

public class GroupPurchaseReindexReader implements ItemReader<GroupPurchaseReindexRow> {

    private final GroupPurchaseReindexRepository repository;
    private final int batchSize;
    private final OffsetDateTime since;

    private long offset = 0;
    private int index = 0;
    private List<GroupPurchaseReindexRow> buffer = List.of();

    public GroupPurchaseReindexReader(GroupPurchaseReindexRepository repository, int batchSize, String since) {
        this.repository = repository;
        this.batchSize = batchSize;
        this.since = since == null ? null : OffsetDateTime.parse(since);
    }

    @Override
    public GroupPurchaseReindexRow read() {
        if (index >= buffer.size()) {
            buffer = fetchBatch();
            index = 0;
            if (buffer.isEmpty()) {
                return null;
            }
        }
        return buffer.get(index++);
    }

    private List<GroupPurchaseReindexRow> fetchBatch() {
        List<GroupPurchaseReindexRow> rows = since == null
                ? repository.fetchAllRows(batchSize, offset)
                : repository.fetchIncrementalRows(since, batchSize, offset);
        offset += batchSize;
        return rows;
    }
}
