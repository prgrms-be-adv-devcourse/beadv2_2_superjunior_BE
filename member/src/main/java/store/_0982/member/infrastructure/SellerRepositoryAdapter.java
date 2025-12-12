package store._0982.member.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.member.domain.Seller;
import store._0982.member.domain.SellerRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SellerRepositoryAdapter implements SellerRepository {
    private final SellerJpaRepository sellerJpaRepository;
    @Override
    public Seller save(Seller seller) {
        return sellerJpaRepository.save(seller);
    }

    @Override
    public Optional<Seller> findById(UUID sellerId) {
        return sellerJpaRepository.findById(sellerId);
    }

    @Override
    public List<Seller> findAllById(List<UUID> sellerIds) {
        return sellerJpaRepository.findAllById(sellerIds);
    }
}
