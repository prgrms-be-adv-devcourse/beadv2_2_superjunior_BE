package store._0982.batch.infrastructure.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import store._0982.batch.domain.cart.Cart;

import java.util.UUID;

public interface CartJpaRepository extends JpaRepository<Cart, UUID> {
    @Modifying
    @Query("delete from Cart c where c.quantity <= 0")
    void deleteAllZeroQuantity();
}
