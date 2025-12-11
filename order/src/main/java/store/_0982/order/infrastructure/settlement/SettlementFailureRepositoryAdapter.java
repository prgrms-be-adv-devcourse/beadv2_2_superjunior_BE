package store._0982.order.infrastructure.settlement;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SettlementFailureRepositoryAdapter {

    private final SettlementJpaRepository settlementJpaRepository;

}
