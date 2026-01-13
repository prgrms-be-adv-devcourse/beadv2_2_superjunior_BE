package store._0982.commerce.application.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceCommand;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceThumbnailInfo;
import store._0982.commerce.domain.sellerbalance.*;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.dto.PageResponse;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceHistoryInfo;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceInfo;
import store._0982.common.exception.CustomException;

import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SellerBalanceService {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @Transactional
    public SellerBalanceThumbnailInfo createSellerBalance(SellerBalanceCommand command) {
        SellerBalance sellerBalance = new SellerBalance(command.sellerId());
        sellerBalanceRepository.save(sellerBalance);
        return SellerBalanceThumbnailInfo.from(sellerBalance);
    }

    public SellerBalanceInfo getBalance(UUID memberId) {
        SellerBalance findSellerBalance = sellerBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SELLER_NOT_FOUND));

        return SellerBalanceInfo.from(findSellerBalance);
    }

    public PageResponse<SellerBalanceHistoryInfo> getBalanceHistory(UUID memberId, Pageable pageable) {
        Page<SellerBalanceHistory> list = sellerBalanceHistoryRepository.findAllMemberId(memberId, pageable);
        Page<SellerBalanceHistoryInfo> sellerBalanceHistoryInfoStream = list.map(SellerBalanceHistoryInfo::from);
        return PageResponse.from(sellerBalanceHistoryInfoStream);
    }

    @Transactional
    public void addFee(UUID memberId, Long fee) {
        SellerBalance findSellerBalance = sellerBalanceRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.SELLER_NOT_FOUND));
        findSellerBalance.increaseBalance(fee);

        // TODO: 상태 값 세분화 예정
        SellerBalanceHistory sellerBalanceHistory =
                new SellerBalanceHistory(memberId, null, null, fee, SellerBalanceHistoryStatus.CREDIT);
        sellerBalanceHistoryRepository.save(sellerBalanceHistory);
    }
}
