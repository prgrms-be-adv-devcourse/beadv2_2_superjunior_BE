package store._0982.elasticsearch.infrastructure.product;

import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductVectorRepository {

    private final JdbcTemplate jdbcTemplate;

    public float[] findVectorByProductId(UUID productId) {
        String sql = "select vector from product_schema.product_vector where product_id = ?";
        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return null;
            }
            Object value = rs.getObject("vector");
            return parseVector(value);
        }, productId);
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
