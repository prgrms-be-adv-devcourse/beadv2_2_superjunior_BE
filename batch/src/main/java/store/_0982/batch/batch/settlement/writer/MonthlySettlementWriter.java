package store._0982.batch.batch.settlement.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.application.BankTransferService;
import store._0982.batch.application.settlement.event.SettlementProcessedEvent;
import store._0982.batch.domain.sellerbalance.*;
import store._0982.batch.domain.settlement.*;
import store._0982.batch.infrastructure.client.member.MemberClient;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountListRequest;
import store._0982.batch.application.settlement.SettlementListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlySettlementWriter implements ItemWriter<Settlement> {

    private final MemberClient memberClient;
    private final BankTransferService bankTransferService;
    private final ApplicationEventPublisher eventPublisher;

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
        List<SellerAccountInfo> accountInfos = memberClient.getSellerAccountInfos(request).data();
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
                    null,
                    transferAmount,
                    SellerBalanceHistoryStatus.DEBIT
            );
            sellerBalanceHistoryRepository.save(history);

            SellerBalance balance = balanceMap.get(settlement.getSellerId());
            balance.resetBalance();
            sellerBalanceRepository.save(balance);

            eventPublisher.publishEvent(
                    new SettlementProcessedEvent(
                            settlement
                    )
            );

            // 정산 성공 로그
//            log.info(BatchLogMetadataFormat.MONTHLY_SETTLEMENT_SUCCESS,
//                    settlement.getSellerId());

        } catch (Exception e) {
            // 정산 실패 로그
//            log.error(BatchLogMetadataFormat.MONTHLY_SETTLEMENT_FAILED,
//                    settlement.getSellerId(),
//                    e.getMessage(),
//                    e);
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

        eventPublisher.publishEvent(
                new SettlementProcessedEvent(
                        settlement
                )
        );
    }
}
