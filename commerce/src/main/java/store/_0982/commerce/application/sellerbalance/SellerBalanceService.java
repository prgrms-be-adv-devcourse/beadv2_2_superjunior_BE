package store._0982.commerce.application.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.dto.PageResponse;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceHistoryInfo;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceInfo;
import store._0982.commerce.domain.settlement.SellerBalance;
import store._0982.commerce.domain.settlement.SellerBalanceHistory;
import store._0982.commerce.domain.settlement.SellerBalanceHistoryRepository;
import store._0982.commerce.domain.settlement.SellerBalanceRepository;

import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SellerBalanceService {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    public SellerBalanceInfo getBalance(UUID memberId) {
        SellerBalance findSellerBalance = sellerBalanceRepository.findByMemberId(memberId)
                .orElse(new SellerBalance(memberId));

        return SellerBalanceInfo.from(findSellerBalance);
    }

    public PageResponse<SellerBalanceHistoryInfo> getBalanceHistory(UUID memberId, Pageable pageable) {
        Page<SellerBalanceHistory> list = sellerBalanceHistoryRepository.findAllMemberId(memberId, pageable);
        Page<SellerBalanceHistoryInfo> sellerBalanceHistoryInfoStream = list.map(SellerBalanceHistoryInfo::from);
        return PageResponse.from(sellerBalanceHistoryInfoStream);
    }
}
