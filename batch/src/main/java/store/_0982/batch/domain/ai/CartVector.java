package store._0982.batch.domain.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartVector extends ProductVector {
    private UUID cartId;
    private int quantity;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
