package store._0982.batch.batch.elasticsearch.reindex.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDocumentEmbedded {

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Keyword)
    private String sellerId;

    public static ProductDocumentEmbedded from(String sellerId, String category, Long price) {
        return new ProductDocumentEmbedded(
                category,
                price,
                sellerId
        );
    }
}
