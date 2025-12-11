package store._0982.order.domain.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "shopping_cart", schema = "order_schema")
public class Cart {

    @Id
    @Column(name = "shopping_cart_id", nullable = false)
    private UUID shoppingCartId;

    @Column(name = "member_id", nullable = false, unique = true)
    private UUID memberId;

    @Column(name = "group_purchase_id", nullable = false)
    private UUID groupPurchaseId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public static Cart create(Integer quantity){
        Cart cart = new Cart();
        cart.shoppingCartId = UUID.randomUUID();
        cart.quantity = Objects.requireNonNullElse(quantity, 1);
        return cart;
    }

    public void delete(){
        this.quantity = 0;
        updatedAt = OffsetDateTime.now();
    }

    public void changeQuantity(int n) {
        this.quantity = n;
        if(this.quantity <= 0){
            this.quantity = 0;
        }
        updatedAt = OffsetDateTime.now();
    }

}
