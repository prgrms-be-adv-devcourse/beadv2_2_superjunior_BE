package store._0982.elasticsearch.infrastructure.search;

import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Repository;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRepository;
import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRow;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class GroupPurchaseSearchRepositoryAdapter implements GroupPurchaseSearchRepository {

    private final GroupPurchaseSearchJpaRepository repository;

    @Override
    public List<GroupPurchaseSearchRow> findAllByIds(List<java.util.UUID> groupPurchaseIds) {
        return repository.findAllByIds(groupPurchaseIds)
                .stream()
                .map(this::toRow)
                .toList();
    }

    private GroupPurchaseSearchRow toRow(GroupPurchaseSearchProjection projection) {
        return new GroupPurchaseSearchRow(
                projection.getGroupPurchaseId(),
                projection.getMinQuantity(),
                projection.getMaxQuantity(),
                projection.getTitle(),
                projection.getDescription(),
                projection.getDiscountedPrice(),
                projection.getStatus(),
                projection.getStartDate(),
                projection.getEndDate(),
                projection.getCreatedAt(),
                projection.getUpdatedAt(),
                projection.getCurrentQuantity(),
                projection.getProductId(),
                projection.getCategory(),
                projection.getPrice(),
                projection.getOriginalUrl(),
                projection.getSellerId(),
                parseVector(projection.getProductVector())
        );
    }

    @SuppressWarnings("java:S1168")
    private float[] parseVector(Object value) {
        if (value == null) {
            return null;
        }
        String raw;
        if (value instanceof PGobject pgObject) {
            raw = pgObject.getValue();
        } else {
            raw = value.toString();
        }
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if ((trimmed.startsWith("[") && trimmed.endsWith("]"))
                || (trimmed.startsWith("(") && trimmed.endsWith(")"))) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        if (trimmed.isEmpty()) {
            return null;
        }
        String[] parts = trimmed.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                return null;
            }
            vector[i] = (float) Double.parseDouble(part);
        }
        return vector;
    }
}
