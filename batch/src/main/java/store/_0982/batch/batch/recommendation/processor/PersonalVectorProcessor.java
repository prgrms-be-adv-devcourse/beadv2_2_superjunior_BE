package store._0982.batch.batch.recommendation.processor;

import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.recommendation.reader.PersonalVectorInfoReader.MemberVectorsInput;
import store._0982.batch.domain.ai.PersonalVector;

import java.util.*;

@Component
@RequiredArgsConstructor
public class PersonalVectorProcessor implements ItemProcessor<MemberVectorsInput, List<PersonalVector>> {

    private static final String ORDER_PRODUCTS_SQL = """
            select
                o.member_id as memberId,
                gp.product_id as productId
            from order_schema."order" o
            join product_schema.group_purchase gp on gp.group_purchase_id = o.group_purchase_id
            where o.member_id in (:memberIds)
            """;

    private static final String PRODUCT_VECTORS_SQL = """
            select
                pv.product_id as productId,
                pv.vector as productVector
            from product_schema.product_vector pv
            where pv.product_id in (:productIds)
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<PersonalVector> process(MemberVectorsInput item) {
        List<UUID> memberIds = item.memberIds();
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of();
        }

        List<OrderProductRow> rows = namedParameterJdbcTemplate.query(
                ORDER_PRODUCTS_SQL,
                new MapSqlParameterSource("memberIds", memberIds),
                (rs, rowNum) -> new OrderProductRow(
                        rs.getObject("memberId", UUID.class),
                        rs.getObject("productId", UUID.class)
                )
        );
        if (rows.isEmpty()) {
            return List.of();
        }

        Map<UUID, List<UUID>> memberToProducts = new LinkedHashMap<>();
        Set<UUID> productIds = new LinkedHashSet<>();
        for (OrderProductRow row : rows) {
            memberToProducts.computeIfAbsent(row.memberId(), key -> new ArrayList<>()).add(row.productId());
            productIds.add(row.productId());
        }

        Map<UUID, float[]> productIdToVector = fetchProductVectors(productIds);
        List<PersonalVector> results = new ArrayList<>(memberIds.size());
        for (UUID memberId : memberIds) {
            List<UUID> memberProductIds = memberToProducts.getOrDefault(memberId, List.of());
            if (memberProductIds.isEmpty()) {
                continue;
            }

            List<float[]> vectors = new ArrayList<>(memberProductIds.size());
            for (UUID productId : memberProductIds) {
                float[] vector = productIdToVector.get(productId);
                if (vector != null) {
                    vectors.add(vector);
                }
            }
            if (vectors.isEmpty()) {
                continue;
            }

            float[] averageVector = VectorUtil.getAverageVector(vectors);
            results.add(PersonalVector.create(memberId, averageVector, null));
        }

        return results;
    }

    private Map<UUID, float[]> fetchProductVectors(Set<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, float[]> vectors = new HashMap<>();
        namedParameterJdbcTemplate.query(
                PRODUCT_VECTORS_SQL,
                new MapSqlParameterSource("productIds", productIds),
                rs -> {
                    UUID productId = rs.getObject("productId", UUID.class);
                    float[] vector = parseVector(rs.getObject("productVector"));
                    if (vector != null) {
                        vectors.put(productId, vector);
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

    private record OrderProductRow(
            UUID memberId,
            UUID productId
    ) {
    }
}
