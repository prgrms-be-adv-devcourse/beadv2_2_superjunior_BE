package store._0982.order.application.settlement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.order.client.dto.SellerAccountInfo;

@Slf4j
@Service
public class BankTransferService {

    @ServiceLog
    public void transfer(SellerAccountInfo sellerAccountInfo, long amount) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
