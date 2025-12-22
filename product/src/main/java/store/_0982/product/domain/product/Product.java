package store._0982.product.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import store._0982.common.kafka.dto.ProductEvent;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "\"product\"", schema = "product_schema")
public class Product {
    @Id
    private UUID productId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductCategory category;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "stock", nullable = false)
    private int stock;

    @Column(name = "original_url")
    private String originalUrl;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public Product(String name,
                   Long price,
                   ProductCategory category,
                   String description,
                   int stock,
                   String originalUrl,
                   UUID sellerId) {

        this.productId = UUID.randomUUID();
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.stock  = stock;
        this.originalUrl = originalUrl;
        this.sellerId = sellerId;
    }

    public void updateProduct(String name,
                              Long price,
                              ProductCategory category,
                              String description,
                              int stock,
                              String originalUrl){
        this.name = name;
        this.price = price;
        this.category = category;
        this.description = description;
        this.stock  = stock;
        this.originalUrl = originalUrl;
    }

    public void softDelete() {
        this.deletedAt = OffsetDateTime.now();
    }

    public ProductEvent toEvent() {
        return new ProductEvent(
                this.productId,
                this.name,
                this.price,
                this.category.name(),
                this.description,
                this.stock,
                this.originalUrl,
                this.sellerId,
                this.createdAt.toString(),
                this.updatedAt != null ? this.updatedAt.toString() : null
        );
    }
}
