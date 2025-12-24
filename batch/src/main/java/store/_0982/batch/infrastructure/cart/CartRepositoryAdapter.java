package store._0982.batch.infrastructure.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.cart.CartRepository;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public void deleteAllZeroQuantity() {
        cartJpaRepository.deleteAllZeroQuantity();
    }

}
