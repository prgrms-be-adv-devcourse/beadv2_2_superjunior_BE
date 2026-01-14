package store._0982.batch.batch.settlement.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.batch.application.settlement.BankTransferService;
import store._0982.batch.application.settlement.SettlementService;
import store._0982.batch.domain.sellerbalance.*;
import store._0982.batch.domain.settlement.*;
import store._0982.batch.infrastructure.client.member.MemberClient;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountListRequest;

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
    private final SettlementService settlementService;

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        List<? extends Settlement> settlements = chunk.getItems();

        // 계좌 정보 일괄 조회
        Map<UUID, SellerAccountInfo> accountMap = fetchSellerAccounts(settlements);

        for (Settlement settlement : settlements) {
            processSettlement(settlement, accountMap);
        }
    }

    private void processSettlement(Settlement settlement, Map<UUID, SellerAccountInfo> accountMap) {
        SellerAccountInfo accountInfo = accountMap.get(settlement.getSellerId());

        if (!isValidAccount(accountInfo)) {
            handleSettlementFailure(settlement, "계좌 정보가 없습니다");
            return;
        }

        try {
            long transferAmount = settlement.getSettlementAmount().longValue();
            bankTransferService.transfer(accountInfo, transferAmount);
        }
        catch (Exception e) {
            handleSettlementFailure(settlement, e.getMessage());
        }
    }

    private Map<UUID, SellerAccountInfo> fetchSellerAccounts(List<? extends Settlement> settlements) {
        List<UUID> sellerIds = settlements.stream()
                .map(Settlement::getSellerId)
                .toList();

        SellerAccountListRequest request = new SellerAccountListRequest(sellerIds);
        return memberClient.getSellerAccountInfos(request)
                .data()
                .stream()
                .collect(Collectors.toMap(SellerAccountInfo::sellerId, Function.identity()));
    }

    private boolean isValidAccount(SellerAccountInfo accountInfo) {
        return accountInfo != null
                && accountInfo.accountNumber() != null
                && !accountInfo.accountNumber().isBlank();
    }

    private void handleSettlementFailure(Settlement settlement, String reason) {
        settlement.markAsFailed();
        settlementService.saveSettlementFailure(settlement, reason);
    }
}
