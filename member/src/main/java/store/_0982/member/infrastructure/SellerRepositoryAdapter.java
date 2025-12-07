package store._0982.member.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.Seller;
import store._0982.member.domain.SellerRepository;

@Repository
@RequiredArgsConstructor
public class SellerRepositoryAdapter implements SellerRepository {
    private SellerJpaRepository sellerJpaRepository;
    @Override
    public Seller save(Seller seller) {
        return sellerJpaRepository.save(seller);
    }
}
