package store._0982.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.OffsetDateTime;

@Builder
@Getter
@Document(indexName = "group-purchase-index", createIndex = false)
// kafka 형식 맞춰서 수정 필요
public class GroupPurchaseDocument {

    @Id
    @Field(type = FieldType.Keyword)
    private String groupPurchaseId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String sellerName;

    @Field(type = FieldType.Integer)
    private Integer minQuantity;

    @Field(type = FieldType.Integer)
    private Integer maxQuantity;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String description;

    @Field(type = FieldType.Long)
    private Long discountedPrice;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String startDate;

    @Field(type = FieldType.Keyword)
    private String endDate;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
    private OffsetDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
    private OffsetDateTime updatedAt;

    @Field(type = FieldType.Integer)
    private Integer currentQuantity;

    @Field(type = FieldType.Nested)
    private ProductDocumentEmbedded productDocumentEmbedded;
}
