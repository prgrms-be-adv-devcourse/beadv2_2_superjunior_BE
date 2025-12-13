package store._0982.elasticsearch.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.application.dto.GroupPurchaseDocumentCommand;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocumentEmbedded {

    @Field(type = FieldType.Keyword)
    private String productId;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Keyword)
    private String originalUrl;

    @Field(type = FieldType.Keyword)
    private String sellerId;

    public static ProductDocumentEmbedded from(ProductEvent event) {
        return new ProductDocumentEmbedded(
                event.getId().toString(),
                event.getCategory(),
                event.getPrice(),
                event.getOriginalUrl(),
                event.getSellerId().toString()
        );
    }
}
