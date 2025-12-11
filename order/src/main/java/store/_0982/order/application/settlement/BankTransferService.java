package store._0982.order.application.settlement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.order.infrastructure.client.dto.SellerAccountInfo;

@Slf4j
@Service
public class BankTransferService {

    public void transfer(SellerAccountInfo sellerAccountInfo, long amount) {
        log.info("[BANK_TRANSFER] 송금 시작 - 계좌: {}, 은행: {}, 예금주: {}, 금액: {}원",
                sellerAccountInfo.accountNumber(),
                sellerAccountInfo.bankCode(),
                sellerAccountInfo.accountHolder(),
                amount);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("[BANK_TRANSFER] 송금 성공 - 계좌: {}, 금액: {}원",
                sellerAccountInfo.accountNumber(), amount);
    }

}
