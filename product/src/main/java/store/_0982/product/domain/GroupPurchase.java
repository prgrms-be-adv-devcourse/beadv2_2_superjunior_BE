package store._0982.product.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@Table(name = "\"group_purchase\"")
public class GroupPurchase {
    @Id
    private UUID productId;

}
