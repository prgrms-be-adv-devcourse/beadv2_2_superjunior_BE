package store._0982.recommendation.infrastructure.elasticsearch;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Builder
@Getter
@Document(indexName = "group-purchase", createIndex = false)
public class GroupPurchaseDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String groupPurchaseId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] productVector;

    @Field(type = FieldType.Nested)
    private ProductDocumentEmbedded productDocumentEmbedded;
}
