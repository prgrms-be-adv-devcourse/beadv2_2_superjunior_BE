package store._0982.order.batch.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.order.application.settlement.BankTransferService;
import store._0982.order.infrastructure.client.member.MemberFeignClient;
import store._0982.order.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.order.infrastructure.client.member.dto.SellerAccountListRequest;
import store._0982.order.domain.settlement.*;
import store._0982.order.infrastructure.settlement.SettlementLogFormat;
import store._0982.order.kafka.event.SettlementEventPublisher;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlySettlementWriter implements ItemWriter<Settlement> {

    private final MemberFeignClient memberFeignClient;
    private final BankTransferService bankTransferService;
    private final SettlementEventPublisher settlementEventPublisher;

    private final SettlementRepository settlementRepository;
    private final SettlementFailureRepository settlementFailureRepository;
    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        List<? extends Settlement> settlements = chunk.getItems();

        List<UUID> sellerIds = settlements.stream()
                .map(Settlement::getSellerId)
                .toList();

        SellerAccountListRequest request = new SellerAccountListRequest(sellerIds);
        List<SellerAccountInfo> accountInfos = memberFeignClient.getSellerAccountInfos(request).data();
        Map<UUID, SellerAccountInfo> accountMap = accountInfos.stream()
                .collect(Collectors.toMap(SellerAccountInfo::sellerId, Function.identity()));

        Map<UUID, SellerBalance> balanceMap = sellerBalanceRepository
                .findAllByMemberIdIn(sellerIds)
                .stream()
                .collect(Collectors.toMap(SellerBalance::getMemberId, Function.identity()));

        for (Settlement settlement : settlements) {
            SellerAccountInfo accountInfo = accountMap.get(settlement.getSellerId());

            if (accountInfo == null || accountInfo.accountNumber() == null || accountInfo.accountNumber().isBlank()) {
                handleSettlementFailure(settlement, "계좌 정보가 없습니다");
                continue;
            }

            processTransfer(settlement, accountInfo, balanceMap);
        }
    }

    private void processTransfer(Settlement settlement, SellerAccountInfo accountInfo, Map<UUID, SellerBalance> balanceMap) {
        try {
            long transferAmount = settlement.getSettlementAmount().longValue();

            bankTransferService.transfer(accountInfo, transferAmount);

            settlement.markAsCompleted();
            settlementRepository.save(settlement);

            SellerBalanceHistory history = new SellerBalanceHistory(
                    settlement.getSellerId(),
                    settlement.getSettlementId(),
                    transferAmount,
                    BalanceHistoryStatus.DEBIT
            );
            sellerBalanceHistoryRepository.save(history);

            SellerBalance balance = balanceMap.get(settlement.getSellerId());
            balance.resetBalance();
            sellerBalanceRepository.save(balance);

            settlementEventPublisher.publishCompleted(settlement);
            log.info(SettlementLogFormat.MONTHLY_SETTLEMENT_COMPLETE, settlement.getSellerId());

        } catch (Exception e) {
            log.error(SettlementLogFormat.MONTHLY_SETTLEMENT_FAIL, settlement.getSellerId(), e.getMessage(), e);
            handleSettlementFailure(settlement, e.getMessage());
        }
    }

    private void handleSettlementFailure(Settlement settlement, String reason) {
        settlement.markAsFailed();
        settlementRepository.save(settlement);

        SettlementFailure failure = new SettlementFailure(
                settlement.getSellerId(),
                settlement.getPeriodStart(),
                settlement.getPeriodEnd(),
                reason,
                0,
                settlement.getSettlementId()
        );
        settlementFailureRepository.save(failure);

        settlementEventPublisher.publishFailed(settlement);
    }
}
