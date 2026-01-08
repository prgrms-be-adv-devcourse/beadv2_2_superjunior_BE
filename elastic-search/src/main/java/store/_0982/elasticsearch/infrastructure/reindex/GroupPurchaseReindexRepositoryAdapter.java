package store._0982.elasticsearch.infrastructure.reindex;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.elasticsearch.domain.reindex.GroupPurchaseReindexRepository;
import store._0982.elasticsearch.domain.reindex.GroupPurchaseReindexRow;

import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class GroupPurchaseReindexRepositoryAdapter implements GroupPurchaseReindexRepository {

    private final GroupPurchaseReindexJpaRepository repository;

    @Override
    public long countSource() {
        return repository.countSource();
    }

    @Override
    public List<GroupPurchaseReindexRow> fetchAllRows(int limit, long offset) {
        return repository.findAllRows(limit, offset)
                .stream()
                .map(GroupPurchaseReindexRow::from)
                .toList();
    }

    @Override
    public List<GroupPurchaseReindexRow> fetchIncrementalRows(OffsetDateTime since, int limit, long offset) {
        return repository.findIncrementalRows(since, limit, offset)
                .stream()
                .map(GroupPurchaseReindexRow::from)
                .toList();
    }
}
