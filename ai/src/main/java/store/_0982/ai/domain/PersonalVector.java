package store._0982.ai.domain;

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

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "personal_vector", schema = "batch_schema")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PersonalVector {

    @Id
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "vector", nullable = false, columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] vector;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public PersonalVector(UUID memberId, float[] vector) {
        this.memberId = memberId;
        this.vector = vector;
    }

    public void updateVector(float[] vector) {
        this.vector = vector;
    }
}
