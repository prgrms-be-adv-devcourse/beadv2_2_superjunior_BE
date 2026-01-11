package store._0982.batch.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.batch.infrastructure.client.member.dto.SellerAccountInfo;
import store._0982.common.log.ServiceLog;

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
