package store._0982.batch.application.sellerpayout;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.batch.application.message.DiscordMessageService;
import store._0982.batch.batch.sellerpayout.dto.SellerAccountDto;
import store._0982.common.log.ServiceLog;

@Slf4j
@RequiredArgsConstructor
@Service
public class BankTransferService {

    private final DiscordMessageService discordMessageService;

    @ServiceLog
    public void transfer(SellerAccountDto sellerAccountDto, long amount) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            discordMessageService.sendDiscordWebhookMessage(
                    new DiscordMessageService.DiscordWebhookMessage(
                            "[ERROR] BankTransferService transfer 에러 : " + sellerAccountDto));

            Thread.currentThread().interrupt();
        }
    }

}
