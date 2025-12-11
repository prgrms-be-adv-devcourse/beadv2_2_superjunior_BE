package store._0982.order.infrastructure.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.order.domain.cart.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartJpaRepository extends JpaRepository<Cart, UUID> {
    @Modifying
    @Query("delete from Cart c where c.quantity <= 0")
    void deleteAllZeroQuantity();
    @Modifying
    @Query("select c from Cart c where c.memberId = :memberId and c.groupPurchaseId = :groupPurchaseid")
    Optional<Cart> findByMemberIdAndGroupPurchaseId(@Param("memberId") UUID memberId,@Param("groupPurchaseId") UUID groupPurchaseId);

}
