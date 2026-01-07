package store._0982.elasticsearch.infrastructure.reindex;

import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public class GroupPurchaseReindexRepository {

    private final GroupPurchaseReindexJpaRepository repository;

    public GroupPurchaseReindexRepository(GroupPurchaseReindexJpaRepository repository) {
        this.repository = repository;
    }

    public long countSource() {
        return repository.countSource();
    }

    public List<GroupPurchaseReindexRow> fetchAllRows(int limit, long offset) {
        return repository.findAllRows(limit, offset)
                .stream()
                .map(GroupPurchaseReindexRow::from)
                .toList();
    }

    public List<GroupPurchaseReindexRow> fetchIncrementalRows(OffsetDateTime since, int limit, long offset) {
        return repository.findIncrementalRows(since, limit, offset)
                .stream()
                .map(GroupPurchaseReindexRow::from)
                .toList();
    }
}
