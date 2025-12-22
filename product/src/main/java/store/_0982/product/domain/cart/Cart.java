package store._0982.product.domain.cart;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "shopping_cart", schema = "order_schema")
public class Cart {

    @Id
    @Column(name = "shopping_cart_id", nullable = false)
    private UUID cartId;

    @Column(name = "member_id", nullable = false)
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

    public static Cart create(UUID memberId, UUID groupPurchaseId){
        Cart cart = new Cart();
        cart.cartId = UUID.randomUUID();
        cart.memberId = memberId;
        cart.groupPurchaseId = groupPurchaseId;
        cart.quantity = 0;
        return cart;
    }

    public void add() {
        this.quantity += 1;
        updatedAt = OffsetDateTime.now();
    }

    public void add(Integer n) {
        if(n == null) {
            add();
            updatedAt = OffsetDateTime.now();
            return;
        }
        this.quantity += n;
        updatedAt = OffsetDateTime.now();
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
