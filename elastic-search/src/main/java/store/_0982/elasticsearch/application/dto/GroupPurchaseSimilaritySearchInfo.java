package store._0982.elasticsearch.application.dto;

import store._0982.elasticsearch.domain.search.GroupPurchaseSearchRow;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public record GroupPurchaseSimilaritySearchInfo(
        String groupPurchaseId,
        Integer minQuantity,
        Integer maxQuantity,
        String title,
        String description,
        Long discountedPrice,
        String status,
        String startDate,
        String endDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Integer currentQuantity,
        Long discountRate,
        Double score,
        ProductSearchInfo productSearchInfo
) {
    public static GroupPurchaseSimilaritySearchInfo from(GroupPurchaseSearchRow row, Double score) {
        return new GroupPurchaseSimilaritySearchInfo(
                row.groupPurchaseId().toString(),
                row.minQuantity(),
                row.maxQuantity(),
                row.title(),
                row.description(),
                row.discountedPrice(),
                row.status(),
                toStringOrNull(row.startDate()),
                toStringOrNull(row.endDate()),
                toOffsetDateTime(row.createdAt()),
                toOffsetDateTime(row.updatedAt()),
                row.currentQuantity(),
                calculateDiscountRate(row.price(), row.discountedPrice()),
                score,
                new ProductSearchInfo(
                        row.productId().toString(),
                        row.category(),
                        row.price(),
                        row.originalUrl(),
                        row.sellerId().toString()
                )
        );
    }

    private static OffsetDateTime toOffsetDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static String toStringOrNull(Instant instant) {
        OffsetDateTime value = toOffsetDateTime(instant);
        return value != null ? value.toString() : null;
    }

    private static long calculateDiscountRate(Long price, Long discountedPrice) {
        if (price == null || discountedPrice == null) {
            return 0L;
        }
        if (price <= 0 || discountedPrice >= price) {
            return 0L;
        }
        return Math.round(((double) (price - discountedPrice) / price) * 100);
    }
}
