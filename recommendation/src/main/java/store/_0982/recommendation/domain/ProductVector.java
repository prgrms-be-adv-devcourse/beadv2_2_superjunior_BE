package store._0982.recommendation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import store._0982.common.kafka.dto.ProductEmbeddingCompletedEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_vector", schema = "product_schema")
public class ProductVector {

    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "vector", nullable = false, columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] vector;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "dimension_size")
    private int dimensionSize;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public ProductVector(UUID productId, float[] vector, String modelVersion) {
        this.productId = productId;
        this.vector = vector;
        this.modelVersion = modelVersion;
        this.dimensionSize = vector == null ? 0 : vector.length;
    }
}
