package store._0982.batch.batch.settlement.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.application.settlement.BankTransferService;
import store._0982.batch.application.settlement.event.SettlementCompletedEvent;
import store._0982.batch.application.settlement.event.SettlementFailedEvent;
import store._0982.batch.domain.settlement.*;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.batch.infrastructure.client.member.MemberClient;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.common.exception.CustomException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementWithdrawalWriter implements ItemWriter<Settlement> {

    private final MemberClient memberClient;
    private final SettlementRepository settlementRepository;
    private final BankTransferService bankTransferService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        List<Settlement> settlements = chunk.getItems().stream()
                .map(s -> (Settlement) s)
                .toList();

        Map<UUID, SellerAccountInfo> accountMap = memberClient.fetchAccounts(settlements);

        for (Settlement settlement : settlements) {
            try {
                SellerAccountInfo accountInfo = accountMap.get(settlement.getSellerId());
                if (!isValidAccount(accountInfo)) {
                    throw new CustomException(CustomErrorCode.INVALID_ACCOUNT_INFO);
                }

                settlement.setAccountInfo(accountInfo.accountNumber(), accountInfo.bankCode());
                bankTransferService.transfer(accountInfo, settlement.getSettlementAmount().longValue());
                settlement.markAsCompleted();
                eventPublisher.publishEvent(new SettlementCompletedEvent(settlement));
            } catch (Exception e) {
                settlement.markAsFailed();
                eventPublisher.publishEvent(new SettlementFailedEvent(settlement, e.getMessage()));
            }
        }

        settlementRepository.saveAll(settlements);
    }

    private boolean isValidAccount(SellerAccountInfo accountInfo) {
        return accountInfo != null
                && accountInfo.accountNumber() != null
                && !accountInfo.accountNumber().isBlank();
    }
}
