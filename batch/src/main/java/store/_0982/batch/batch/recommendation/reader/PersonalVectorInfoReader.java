package store._0982.batch.batch.recommendation.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.postgresql.util.PGobject;
import store._0982.common.domain.vector.ProductVector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@StepScope
@Slf4j
public class PersonalVectorInfoReader implements ItemReader<List<PersonalVectorInfoReader.MemberVectorsInput>> {

    @Value("${vector.batch.size}")
    private static int pageSize;

    private static final String MEMBER_IDS_SQL = """
            select distinct member_id
            from order_schema."order"
            order by member_id
            limit ? offset ?
            """;

    private static final String ORDERS_SQL = """
            select
                o.member_id as memberId,
                o.group_purchase_id as groupPurchaseId
            from order_schema."order" o
            where o.member_id in (:memberIds)
            """;

    private static final String PRODUCTS_SQL = """
            select
                gp.group_purchase_id as groupPurchaseId,
                gp.product_id as productId
            from product_schema."group_purchase" gp
            where gp.group_purchase_id in (:groupPurchaseIds)
            """;

    private static final String PRODUCT_VECTORS_SQL = """
            select
                pv.product_id as productId,
                pv.vector as productVector
            from recommendation_schema.product_vector pv
            where pv.product_id in (:productIds)
            """;

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private long offset = 0L;

    @Override
    public List<MemberVectorsInput> read() {
        List<UUID> memberIds = jdbcTemplate.query(
                MEMBER_IDS_SQL,
                (rs, rowNum) -> rs.getObject("member_id", UUID.class),
                pageSize,
                offset
        );
        if (memberIds.isEmpty()) {
            return null;
        }
        offset += pageSize;

        List<OrderRow> orderRows = namedParameterJdbcTemplate.query(
                ORDERS_SQL,
                new MapSqlParameterSource("memberIds", memberIds),
                (rs, rowNum) -> new OrderRow(
                        rs.getObject("memberId", UUID.class),
                        rs.getObject("groupPurchaseId", UUID.class)
                )
        );
        if (orderRows.isEmpty()) {
            return memberIds.stream()
                    .map(memberId -> new MemberVectorsInput(memberId, List.of()))
                    .toList();
        }

        Map<UUID, List<UUID>> memberToGroupPurchases = new LinkedHashMap<>();
        Set<UUID> groupPurchaseIds = new LinkedHashSet<>();
        for (OrderRow row : orderRows) {
            if (row.groupPurchaseId() == null || row.memberId() == null) {
                continue;
            }
            memberToGroupPurchases
                    .computeIfAbsent(row.memberId(), key -> new ArrayList<>())
                    .add(row.groupPurchaseId());
            groupPurchaseIds.add(row.groupPurchaseId());
        }
        if (groupPurchaseIds.isEmpty()) {
            return memberIds.stream()
                    .map(memberId -> new MemberVectorsInput(memberId, List.of()))
                    .toList();
        }

        List<GroupPurchaseProductRow> groupPurchaseRows = namedParameterJdbcTemplate.query(
                PRODUCTS_SQL,
                new MapSqlParameterSource("groupPurchaseIds", groupPurchaseIds),
                (rs, rowNum) -> new GroupPurchaseProductRow(
                        rs.getObject("groupPurchaseId", UUID.class),
                        rs.getObject("productId", UUID.class)
                )
        );
        if (groupPurchaseRows.isEmpty()) {
            return memberIds.stream()
                    .map(memberId -> new MemberVectorsInput(memberId, List.of()))
                    .toList();
        }

        Map<UUID, UUID> groupPurchaseToProduct = new HashMap<>();
        for (GroupPurchaseProductRow row : groupPurchaseRows) {
            if (row.groupPurchaseId() == null || row.productId() == null) {
                continue;
            }
            groupPurchaseToProduct.put(row.groupPurchaseId(), row.productId());
        }

        Map<UUID, List<UUID>> memberToProductIds = new LinkedHashMap<>();
        Set<UUID> productIds = new LinkedHashSet<>();
        for (Map.Entry<UUID, List<UUID>> entry : memberToGroupPurchases.entrySet()) {
            UUID memberId = entry.getKey();
            for (UUID groupPurchaseId : entry.getValue()) {
                UUID productId = groupPurchaseToProduct.get(groupPurchaseId);
                if (productId == null) {
                    continue;
                }
                memberToProductIds
                        .computeIfAbsent(memberId, key -> new ArrayList<>())
                        .add(productId);
                productIds.add(productId);
            }
        }

        Map<UUID, ProductVector> productIdToVector = fetchProductVectors(productIds);
        return memberIds.stream()
                .map(memberId -> new MemberVectorsInput(
                        memberId,
                        buildMemberVectors(memberToProductIds.get(memberId), productIdToVector)
                ))
                .toList();
    }

    private List<ProductVector> buildMemberVectors(
            List<UUID> productIds,
            Map<UUID, ProductVector> productIdToVector
    ) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        List<ProductVector> vectors = new ArrayList<>(productIds.size());
        for (UUID productId : productIds) {
            ProductVector vector = productIdToVector.get(productId);
            if (vector != null) {
                vectors.add(vector);
            }
        }
        return vectors;
    }

    private Map<UUID, ProductVector> fetchProductVectors(Set<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, ProductVector> vectors = new HashMap<>();
        namedParameterJdbcTemplate.query(
                PRODUCT_VECTORS_SQL,
                new MapSqlParameterSource("productIds", productIds),
                rs -> {
                    UUID productId = rs.getObject("productId", UUID.class);
                    float[] vector = parseVector(rs.getObject("productVector"));
                    if (productId != null && vector != null) {
                        vectors.put(productId, createProductVector(productId, vector));
                    }
                }
        );
        return vectors;
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

    private static ProductVector createProductVector(UUID productId, float[] vector) {
        try {
            Constructor<ProductVector> ctor = ProductVector.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ProductVector productVector = ctor.newInstance();
            setField(productVector, "productId", productId);
            setField(productVector, "vector", vector);
            if (vector != null) {
                setField(productVector, "dimensionSize", vector.length);
            }
            return productVector;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to build ProductVector instance.", e);
        }
    }

    @SuppressWarnings("java:S3011")
    private static void setField(ProductVector target, String fieldName, Object value)
            throws ReflectiveOperationException {
        Field field = ProductVector.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    public record MemberVectorsInput(
            UUID memberId,
            List<ProductVector> productVectors
    ) {
    }

    private record OrderRow(
            UUID memberId,
            UUID groupPurchaseId
    ) {
    }

    private record GroupPurchaseProductRow(
            UUID groupPurchaseId,
            UUID productId
    ) {
    }
}
