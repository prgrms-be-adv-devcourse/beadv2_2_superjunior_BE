package store._0982.batch.domain.sellerbalance;

import store._0982.common.domain.sellerbalance.SellerBalanceHistory;

import java.util.List;

public interface SellerBalanceHistoryRepository {

    void save(SellerBalanceHistory sellerBalanceHistory);

    void saveAll(List<SellerBalanceHistory> sellerBalanceHistories);
}
