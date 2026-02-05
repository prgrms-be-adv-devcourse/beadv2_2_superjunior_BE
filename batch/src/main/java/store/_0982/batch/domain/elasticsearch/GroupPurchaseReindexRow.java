package store._0982.batch.domain.elasticsearch;

import store._0982.batch.infrastructure.elasticsearch.GroupPurchaseReindexProjection;

import java.time.Instant;
import java.util.UUID;

import org.postgresql.util.PGobject;

public record GroupPurchaseReindexRow(
        UUID groupPurchaseId,
        String title,
        String description,
        String status,
        Instant endDate,
        long discountedPrice,
        Integer currentQuantity,
        Instant updatedAt,
        UUID productId,
        String category,
        Long price,
        UUID sellerId,
        float[] productVector
) {
    public static GroupPurchaseReindexRow from(GroupPurchaseReindexProjection projection) {
        return new GroupPurchaseReindexRow(
                projection.getGroupPurchaseId(),
                projection.getTitle(),
                projection.getDescription(),
                projection.getStatus(),
                projection.getEndDate(),
                projection.getDiscountedPrice(),
                projection.getCurrentQuantity(),
                projection.getUpdatedAt(),
                projection.getProductId(),
                projection.getCategory(),
                projection.getPrice(),
                projection.getSellerId(),
                parseVector(projection.getProductVector())
        );
    }

    @SuppressWarnings("java:S1168")
    private static float[] parseVector(Object value) {
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
