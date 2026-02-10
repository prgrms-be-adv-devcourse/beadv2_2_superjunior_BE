package store._0982.batch.infrastructure.recommendation;

import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductVectorQueryRepositoryAdapter implements ProductVectorQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Map<UUID, float[]> findVectorsByProductIds(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        String sql = """
                select product_id, vector
                from recommendation_schema.product_vector
                where product_id in (:ids)
                """;
        MapSqlParameterSource params = new MapSqlParameterSource("ids", productIds);
        Map<UUID, float[]> result = new HashMap<>();
        jdbcTemplate.query(sql, params, rs -> {
            UUID productId = rs.getObject("product_id", UUID.class);
            Object vectorValue = rs.getObject("vector");
            result.put(productId, parseVector(vectorValue));
        });
        return result;
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
