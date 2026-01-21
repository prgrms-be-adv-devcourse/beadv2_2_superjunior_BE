package store._0982.batch.infrastructure.elasticsearch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRepository;
import store._0982.batch.domain.elasticsearch.GroupPurchaseReindexRow;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

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

    @Override
    public List<GroupPurchaseReindexRow> fetchByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findRowsByIds(ids)
                .stream()
                .map(GroupPurchaseReindexRow::from)
                .toList();
    }
}
