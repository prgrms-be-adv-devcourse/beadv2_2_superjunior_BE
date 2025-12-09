package store._0982.order.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.order.domain.settlement.SellerBalanceRepository;

@RequiredArgsConstructor
@Repository
public class SellerBalanceRepositoryAdapter implements SellerBalanceRepository {

    private final SellerBalanceJpaRepository sellerBalanceJpaRepository;

}
