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
import store._0982.batch.application.sellerpayout.event.SellerPayoutFailedEvent;
import store._0982.batch.batch.sellerpayout.dto.SellerAccountDto;
import store._0982.batch.domain.sellerpayout.SellerPayoutFailureRepository;
import store._0982.batch.domain.sellerpayout.SellerPayoutRepository;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.batch.infrastructure.seller.SellerAccountJdbcRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.exception.CustomException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RetryFailedSellerPayoutWriter implements ItemWriter<SellerPayout> {

    private final SellerAccountJdbcRepository sellerAccountJdbcRepository;
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

        List<UUID> sellerIds = sellerPayouts.stream()
                .map(SellerPayout::getSellerId)
                .toList();

        Map<UUID, SellerAccountDto> accountMap = sellerAccountJdbcRepository.findAccountsBySellerIds(sellerIds);

        for (SellerPayout sellerPayout : sellerPayouts) {
            try {
                SellerAccountDto accountDto = accountMap.get(sellerPayout.getSellerId());
                if (!isValidAccount(accountDto)) {
                    throw new CustomException(CustomErrorCode.INVALID_ACCOUNT_INFO);
                }

                sellerPayout.setAccountInfo(accountDto.accountNumber(), accountDto.bankCode());
                bankTransferService.transfer(accountDto, sellerPayout.getTotalAmount());
                sellerPayout.markAsCompleted();
                sellerBalanceService.clearBalance(sellerPayout);

                sellerPayoutFailureRepository.deleteBySellerPayoutId(sellerPayout.getSellerPayoutId());

                eventPublisher.publishEvent(new SellerPayoutCompletedEvent(sellerPayout));
            } catch (CustomException e) {
                sellerPayout.markAsFailed();
                sellerPayoutFailureRepository.incrementRetryCount(sellerPayout.getSellerPayoutId());

                eventPublisher.publishEvent(new SellerPayoutFailedEvent(sellerPayout, e.getMessage()));
            }
        }

        sellerPayoutRepository.saveAll(sellerPayouts);
    }

    private boolean isValidAccount(SellerAccountDto accountDto) {
        return accountDto != null
                && accountDto.accountNumber() != null
                && !accountDto.accountNumber().isBlank();
    }
}
