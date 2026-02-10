package store._0982.common.domain.vector;

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
@Table(name = "product_vector", schema = "recommendation_schema")
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

    public ProductVector(UUID productId, float[] embedding, String currentModelVersion) {
        this.productId = productId;
        this.vector = embedding;
        this.modelVersion = currentModelVersion;
        this.dimensionSize = embedding == null ? 0 : embedding.length;
    }
}
