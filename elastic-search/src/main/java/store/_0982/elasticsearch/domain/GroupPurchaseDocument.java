package store._0982.elasticsearch.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import store._0982.elasticsearch.domain.reindex.GroupPurchaseReindexRow;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Builder
@Getter
@Document(indexName = "group-purchase", createIndex = false)
// kafka 형식 맞춰서 수정 필요
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

    @Field(type = FieldType.Long)
    private Long discountedPrice;

    @Field(type = FieldType.Integer)
    private Integer currentQuantity;

    @Field(type = FieldType.Date, format = DateFormat.strict_date_optional_time_nanos)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
    private OffsetDateTime endDate;

    @Field(type = FieldType.Date, format = DateFormat.strict_date_optional_time_nanos)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX")
    private OffsetDateTime updatedAt;

    @Field(type = FieldType.Long)
    private Long discountRate;

    @Field(type = FieldType.Dense_Vector, dims = 1536)
    private float[] productVector;

    @Field(type = FieldType.Nested)
    private ProductDocumentEmbedded productDocumentEmbedded;

    public static GroupPurchaseDocument fromReindexRow(GroupPurchaseReindexRow row) {
        return GroupPurchaseDocument.builder()
                .groupPurchaseId(row.groupPurchaseId().toString())
                .title(row.title())
                .description(row.description())
                .status(row.status())
                .discountedPrice(row.discountedPrice())
                .currentQuantity(row.currentQuantity())
                .endDate(toOffsetDateTime(row.endDate()))
                .updatedAt(toOffsetDateTime(row.updatedAt()))
                .discountRate(calculateDiscountRate(row.price(), row.discountedPrice()))
                .productVector(row.productVector())
                .productDocumentEmbedded(new ProductDocumentEmbedded(
                        toStringOrNull(row.productId()),
                        row.category(),
                        row.price(),
                        row.sellerId().toString()
                ))
                .build();
    }

    private static OffsetDateTime toOffsetDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static long calculateDiscountRate(Long price, Long discountedPrice) {
        if (price == null || discountedPrice == null) {
            return 0L;
        }
        if (price <= 0 || discountedPrice >= price) {
            return 0L;
        }
        return Math.round(((double) (price - discountedPrice) / price) * 100);
    }

    private static String toStringOrNull(Object value) {
        return value == null ? null : value.toString();
    }

}
