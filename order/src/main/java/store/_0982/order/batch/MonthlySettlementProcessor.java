package store._0982.order.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import store._0982.order.application.settlement.BankTransferService;
import store._0982.order.client.dto.SellerAccountInfo;
import store._0982.order.domain.settlement.*;
import store._0982.order.infrastructure.settlement.SettlementLogFormat;
import store._0982.order.infrastructure.settlement.event.SettlementEventPublisher;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonthlySettlementProcessor {

    private final BankTransferService bankTransferService;
    private final SettlementEventPublisher settlementEventPublisher;

    private final SettlementRepository settlementRepository;
    private final SettlementFailureRepository settlementFailureRepository;
    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    /**
     * 정산 송금 처리
     * - 은행 송금 실행
     * - Settlement 완료 처리
     * - SellerBalance 리셋
     * - History 기록
     * - 이벤트 발행
     */
    public void processTransfer(Settlement settlement, SellerAccountInfo accountInfo, Map<UUID, SellerBalance> balanceMap) {
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

    /**
     * 정산 실패 처리
     * - Settlement 실패 상태 변경
     * - SettlementFailure 기록
     * - 실패 이벤트 발행
     */
    public void handleSettlementFailure(Settlement settlement, String reason) {
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
