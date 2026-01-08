package store._0982.elasticsearch.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import store._0982.common.kafka.dto.ProductEvent;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocumentEmbedded {

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String sellerId;

    public static ProductDocumentEmbedded from(ProductEvent event) {
        return new ProductDocumentEmbedded(
                event.getCategory(),
                event.getSellerId().toString()
        );
    }
}
