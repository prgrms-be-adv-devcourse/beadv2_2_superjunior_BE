package store._0982.batch.batch.sellerpayout.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.application.sellerbalance.SellerBalanceService;
import store._0982.batch.application.sellerpayout.BankTransferService;
import store._0982.batch.application.sellerpayout.event.SellerPayoutCompletedEvent;
import store._0982.batch.domain.sellerpayout.SellerPayoutFailureRepository;
import store._0982.batch.domain.sellerpayout.SellerPayoutRepository;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.batch.infrastructure.client.member.MemberClient;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.domain.sellerpayout.SellerPayoutFailure;
import store._0982.common.exception.CustomException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerPayoutWriter implements ItemWriter<SellerPayout> {

    private final MemberClient memberClient;
    private final SellerPayoutRepository sellerPayoutRepository;
    private final SellerPayoutFailureRepository sellerPayoutFailureRepository;

    private final BankTransferService bankTransferService;
    private final SellerBalanceService sellerBalanceService;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends SellerPayout> chunk) {
        List<SellerPayout> sellerPayouts = chunk.getItems().stream()
                .map(s -> (SellerPayout) s)
                .toList();

        Map<UUID, SellerAccountInfo> accountMap = memberClient.fetchAccounts(sellerPayouts);

        List<SellerPayoutFailure> failures = new ArrayList<>();
        for (SellerPayout sellerPayout : sellerPayouts) {
            try {
                SellerAccountInfo accountInfo = accountMap.get(sellerPayout.getSellerId());
                if (!isValidAccount(accountInfo)) {
                    throw new CustomException(CustomErrorCode.INVALID_ACCOUNT_INFO);
                }

                sellerPayout.setAccountInfo(accountInfo.accountNumber(), accountInfo.bankCode());
                bankTransferService.transfer(accountInfo, sellerPayout.getTotalAmount());
                sellerPayout.markAsCompleted();
                sellerBalanceService.clearBalance(sellerPayout);

                eventPublisher.publishEvent(new SellerPayoutCompletedEvent(sellerPayout));
            } catch (CustomException e) {
                sellerPayout.markAsFailed();
                failures.add(SellerPayoutFailure.createSellerPayoutFailure(
                        sellerPayout.getSellerPayoutId(),
                        sellerPayout.getSellerId(),
                        sellerPayout.getPeriodStart(),
                        sellerPayout.getPeriodEnd(),
                        e.getMessage(),
                        0
                ));
            }
        }

        sellerPayoutRepository.saveAll(sellerPayouts);
        if (!failures.isEmpty()) {
            sellerPayoutFailureRepository.saveAll(failures);
        }
    }

    private boolean isValidAccount(SellerAccountInfo accountInfo) {
        return accountInfo != null
                && accountInfo.accountNumber() != null
                && !accountInfo.accountNumber().isBlank();
    }
}
