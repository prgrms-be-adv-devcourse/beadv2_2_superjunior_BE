package store._0982.recommendation.infrastructure.elasticsearch;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;
import store._0982.recommendation.application.SearchQueryPort;
import store._0982.recommendation.application.dto.ProductSearchInfo;
import store._0982.recommendation.application.dto.VectorSearchRequest;
import store._0982.recommendation.application.dto.VectorSearchResponse;
import store._0982.recommendation.infrastructure.elasticsearch.query.GroupPurchaseSearchWithEmbeddingQueryFactory;
import store._0982.recommendation.infrastructure.feign.commerce.CommerceProductFeignClient;
import store._0982.recommendation.infrastructure.feign.commerce.dto.GroupPurchaseIdsRequest;
import store._0982.recommendation.infrastructure.feign.commerce.dto.GroupPurchaseSearchRow;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ElasticsearchSearchQueryAdapter implements SearchQueryPort {

    private static final String DEFAULT_STATUS = "OPEN";

    private final ElasticsearchOperations operations;
    private final GroupPurchaseSearchWithEmbeddingQueryFactory groupPurchaseSearchWithEmbeddingQueryFactory;
    private final CommerceProductFeignClient commerceProductFeignClient;

    @Override
    public List<VectorSearchResponse> getRecommandationCandidates(VectorSearchRequest request) {
        if (request == null || request.topK() <= 0) {
            return List.of();
        }
        if (request.vector() == null || request.vector().length == 0) {
            throw new IllegalArgumentException("vector is null or empty");
        }

        Pageable vectorPageable = PageRequest.of(0, request.topK());
        var knnQuery = groupPurchaseSearchWithEmbeddingQueryFactory.createKnnQuery(
                request.vector(),
                "",
                vectorPageable);
        SearchHits<GroupPurchaseDocument> hits = operations.search(knnQuery, GroupPurchaseDocument.class);
        List<UUID> orderedIds = hits.getSearchHits()
                .stream()
                .map(hit -> UUID.fromString(hit.getId()))
                .toList();
        if (orderedIds.isEmpty()) {
            return List.of();
        }

        List<GroupPurchaseSearchRow> rows = commerceProductFeignClient.findPurchasesByIds(new GroupPurchaseIdsRequest(orderedIds));
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        Map<UUID, GroupPurchaseSearchRow> rowMap = rows.stream()
                .collect(Collectors.toMap(GroupPurchaseSearchRow::groupPurchaseId, Function.identity()));

        List<VectorSearchResponse> ordered = new ArrayList<>(orderedIds.size());
        for (UUID id : orderedIds) {
            GroupPurchaseSearchRow row = rowMap.get(id);
            if (row != null) {
                ordered.add(toVectorSearchResponse(row));
            }
        }
        return ordered;
    }

    private VectorSearchResponse toVectorSearchResponse(GroupPurchaseSearchRow row) {
        return new VectorSearchResponse(
                row.groupPurchaseId(),
                row.minQuantity(),
                row.maxQuantity(),
                row.title(),
                row.description(),
                row.imageUrl(),
                row.discountedPrice(),
                row.status(),
                toStringOrNull(row.startDate()),
                toStringOrNull(row.endDate()),
                toOffsetDateTime(row.createdAt()),
                toOffsetDateTime(row.updatedAt()),
                row.currentQuantity(),
                calculateDiscountRate(row.price(), row.discountedPrice()),
                new ProductSearchInfo(
                        row.productId().toString(),
                        row.category(),
                        row.price(),
                        row.originalUrl(),
                        row.sellerId().toString()
                ),
                null
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
