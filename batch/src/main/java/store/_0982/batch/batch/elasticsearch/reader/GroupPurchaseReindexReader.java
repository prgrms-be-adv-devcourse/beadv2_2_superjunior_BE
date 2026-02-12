package store._0982.batch.batch.elasticsearch.reader;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRepository;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRow;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.time.ZoneOffset;

public class GroupPurchaseReindexReader implements ItemReader<GroupPurchaseReindexRow>, ItemStream {

    private final GroupPurchaseReindexRepository repository;
    private final int batchSize;
    private final OffsetDateTime since;

    private int index = 0;
    private List<GroupPurchaseReindexRow> buffer = List.of();
    private UUID lastId = null;
    private OffsetDateTime lastUpdatedAt = null;

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

    @Override
    public void open(ExecutionContext executionContext) {
        if (executionContext.containsKey("lastId")) {
            String lastIdValue = executionContext.getString("lastId");
            lastId = lastIdValue == null ? null : UUID.fromString(lastIdValue);
        }
        if (executionContext.containsKey("lastUpdatedAt")) {
            String lastUpdatedAtValue = executionContext.getString("lastUpdatedAt");
            lastUpdatedAt = lastUpdatedAtValue == null ? null : OffsetDateTime.parse(lastUpdatedAtValue);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        if (lastId != null) {
            executionContext.putString("lastId", lastId.toString());
        }
        if (lastUpdatedAt != null) {
            executionContext.putString("lastUpdatedAt", lastUpdatedAt.toString());
        }
    }

    @Override
    public void close() {
        // no-op
    }

    private List<GroupPurchaseReindexRow> fetchBatch() {
        List<GroupPurchaseReindexRow> rows = since == null
                ? repository.fetchAllRows(batchSize, lastId)
                : repository.fetchIncrementalRows(since, lastUpdatedAt, lastId, batchSize);
        if (!rows.isEmpty()) {
            GroupPurchaseReindexRow lastRow = rows.get(rows.size() - 1);
            lastId = lastRow.groupPurchaseId();
            if (lastRow.updatedAt() != null) {
                lastUpdatedAt = OffsetDateTime.ofInstant(lastRow.updatedAt(), ZoneOffset.UTC);
            }
        }
        return rows;
    }
}
