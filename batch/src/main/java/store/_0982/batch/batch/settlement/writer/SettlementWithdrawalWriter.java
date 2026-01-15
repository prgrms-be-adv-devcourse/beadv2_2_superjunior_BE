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
import store._0982.batch.exception.CustomErrorCode;
import store._0982.batch.infrastructure.client.member.MemberClient;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountListRequest;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementWithdrawalWriter implements ItemWriter<Settlement> {

    private final MemberClient memberClient;
    private final SettlementRepository settlementRepository;

    private final BankTransferService bankTransferService;
    private final SettlementService settlementService;

    @Override
    public void write(Chunk<? extends Settlement> chunk) {
        List<Settlement> settlements = chunk.getItems().stream()
                .map(settlement -> (Settlement) settlement)
                .toList();

        // 계좌 정보 일괄 조회
        Map<UUID, SellerAccountInfo> accountMap;
        try {
            accountMap = fetchSellerAccounts(settlements);
        } catch (Exception e) {
            settlementService.saveAllSettlementFailures(
                    settlements,
                    "Member 서비스를 사용할 수 없습니다. : " + e.getMessage()
            );
            throw new CustomException(CustomErrorCode.MEMBER_SERVICE_UNAVAILABLE);
        }

        for (Settlement settlement : settlements) {
            processSettlement(settlement, accountMap);
        }

        settlementRepository.saveAll(settlements);
    }

    private void processSettlement(Settlement settlement, Map<UUID, SellerAccountInfo> accountMap) {
        SellerAccountInfo accountInfo = accountMap.get(settlement.getSellerId());

        if (!isValidAccount(accountInfo)) {
            settlement.markAsFailed();
            handleSettlementFailure(settlement, "계좌 정보가 없습니다");
            return;
        }

        settlement.setAccountInfo(accountInfo.accountNumber(), accountInfo.bankCode());

        try {
            long transferAmount = settlement.getSettlementAmount().longValue();
            bankTransferService.transfer(accountInfo, transferAmount);
        }
        catch (Exception e) {
            settlement.markAsFailed();
            handleSettlementFailure(settlement, e.getMessage());
        }
    }

    private Map<UUID, SellerAccountInfo> fetchSellerAccounts(List<? extends Settlement> settlements) {
        List<UUID> sellerIds = settlements.stream()
                .map(Settlement::getSellerId)
                .toList();

        SellerAccountListRequest request = new SellerAccountListRequest(sellerIds);

        ResponseDto<List<SellerAccountInfo>> response = memberClient.getSellerAccountInfos(request);
        if (response == null || response.data() == null) {
            return Collections.emptyMap();
        }

        return response
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
        settlementService.saveSettlementFailure(settlement, reason);
    }
}
