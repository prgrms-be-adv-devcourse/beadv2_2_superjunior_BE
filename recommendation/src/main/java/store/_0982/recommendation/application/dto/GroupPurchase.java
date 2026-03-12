package store._0982.recommendation.application.dto;

import store._0982.recommendation.infrastructure.feign.search.dto.ProductSearchInfo;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record GroupPurchase(
    UUID groupPurchaseId,
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
    public static GroupPurchase from(VectorSearchResponse vectorSearchResponse) {
        return new GroupPurchase(
                vectorSearchResponse.groupPurchaseId(),
                vectorSearchResponse.minQuantity(),
                vectorSearchResponse.maxQuantity(),
                vectorSearchResponse.title(),
                vectorSearchResponse.description(),
                vectorSearchResponse.imageUrl(),
                vectorSearchResponse.discountedPrice(),
                vectorSearchResponse.status(),
                vectorSearchResponse.startDate(),
                vectorSearchResponse.endDate(),
                vectorSearchResponse.createdAt(),
                vectorSearchResponse.updatedAt(),
                vectorSearchResponse.currentQuantity(),
                vectorSearchResponse.discountRate(),
                vectorSearchResponse.productSearchInfo()
        );
    }

    public static GroupPurchase from(Map<String, Object> metadata) {
        return new GroupPurchase(
                parseUUID(metadata.get("groupPurchaseId")),
                parseInteger(metadata.get("minQuantity")),
                parseInteger(metadata.get("maxQuantity")),
                toString(metadata.get("title")),
                toString(metadata.get("description")),
                toString(metadata.get("imageUrl")),
                parseLong(metadata.get("discountedPrice")),
                toString(metadata.get("status")),
                toString(metadata.get("startDate")),
                toString(metadata.get("endDate")),
                null,  // createdAt  (metadata에 저장하지 않음)
                null,  // updatedAt
                parseInteger(metadata.get("currentQuantity")),
                parseLong(metadata.get("discountRate")),
                null   // productSearchInfo (metadata에 저장하지 않음)
        );
    }

    private static UUID parseUUID(Object value) {
        if (value == null) return null;
        if (value instanceof UUID uuid) return uuid;
        try { return UUID.fromString(value.toString()); }
        catch (Exception e) { return null; }
    }

    private static Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number num) return num.intValue();
        try { return Integer.parseInt(value.toString()); }
        catch (Exception e) { return null; }
    }

    private static Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number num) return num.longValue();
        try { return Long.parseLong(value.toString()); }
        catch (Exception e) { return null; }
    }

    private static String toString(Object value) {
        return value != null ? value.toString() : null;
    }
}
