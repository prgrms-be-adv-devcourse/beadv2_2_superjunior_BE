package store._0982.elasticsearch.domain.reindex;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "\"group_purchase\"", schema = "product_schema")
public class GroupPurchaseReadEntity {
    @Id
    @Column(name = "group_purchase_id")
    private UUID groupPurchaseId;
}
