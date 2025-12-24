package store._0982.commerce.infrastructure.cart;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.commerce.domain.cart.Cart;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartJpaRepository extends JpaRepository<Cart, UUID> {
    @Modifying
    @Query("delete from Cart c where c.quantity <= 0")
    void deleteAllZeroQuantity();

    Optional<Cart> findByMemberIdAndGroupPurchaseId(@Param("memberId") UUID memberId, @Param("groupPurchaseId") UUID groupPurchaseId);

    @Query("select c from Cart c where c.memberId = :memberId and c.quantity > 0")
    Page<Cart> findAllByMemberId(@Param("memberId") UUID memberId, Pageable pageable);

    @Modifying
    @Query("update Cart c set c.quantity = 0 where c.memberId = :memberId")
    void flushCart(UUID memberId);

    List<Cart> findAllByCartIdIn(List<UUID> cartIds);
}
