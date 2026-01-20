package store._0982.batch.domain.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "ai_schema", name = "personal_vector")
@NoArgsConstructor
public class PersonalVector {
    @Id
    @Column(name = "member_id")
    private UUID memberId;

    @Column(name = "vector", nullable = false, columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] vector;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updateAt;

    private PersonalVector(UUID memberId, float[] vector) {
        this.memberId = memberId;
        this.vector = vector;
    }
    public static PersonalVector create(UUID memberId, float[] vector) {
        return new PersonalVector(memberId, vector);
    }
}
