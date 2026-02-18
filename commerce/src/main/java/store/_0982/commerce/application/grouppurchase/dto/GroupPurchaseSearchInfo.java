package store._0982.commerce.application.grouppurchase.dto;

import java.time.OffsetDateTime;

public record GroupPurchaseSearchInfo(
        String groupPurchaseId,
        Integer minQuantity,
        Integer maxQuantity,
        String title,
        String description,
        String imageUrl,
        Long discountedPrice,
        String status,
        String startDate,
        String endDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Integer currentQuantity,
        Long discountRate,
        ProductSearchInfo productSearchInfo
) {
    public static GroupPurchaseSearchInfo from(GroupPurchaseSearchRow row) {
        return new GroupPurchaseSearchInfo(
                row.groupPurchaseId().toString(),
                row.minQuantity(),
                row.maxQuantity(),
                row.title(),
                row.description(),
                row.imageUrl(),
                row.discountedPrice(),
                row.status().name(),
                toStringOrNull(row.startDate()),
                toStringOrNull(row.endDate()),
                row.createdAt(),
                row.updatedAt(),
                row.currentQuantity(),
                calculateDiscountRate(row.price(), row.discountedPrice()),
                new ProductSearchInfo(
                        row.productId().toString(),
                        row.category().name(),
                        row.price(),
                        row.originalUrl(),
                        row.sellerId().toString()
                )
        );
    }

    private static String toStringOrNull(OffsetDateTime value) {
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
