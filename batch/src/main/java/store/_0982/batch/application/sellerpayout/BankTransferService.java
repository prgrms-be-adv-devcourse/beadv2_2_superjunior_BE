package store._0982.batch.application.sellerpayout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.batch.batch.sellerpayout.dto.SellerAccountDto;
import store._0982.common.log.ServiceLog;

@Slf4j
@Service
public class BankTransferService {

    @ServiceLog
    public void transfer(SellerAccountDto sellerAccountDto, long amount) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
