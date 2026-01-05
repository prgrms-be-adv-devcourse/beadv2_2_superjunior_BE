package store._0982.commerce.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store._0982.commerce.application.sellerbalance.DailySellerBalanceService;
import store._0982.commerce.domain.sellerbalance.SellerBalanceLogFormat;

@Slf4j
@RequiredArgsConstructor
@Component
class DailySellerBalanceScheduler {

    private final DailySellerBalanceService dailySellerBalanceService;

    /**
     * 매일 01:00에 데일리 정산 실행
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void scheduleDailySettlement() {
        String schedulerName = "DailySettlement";
        log.info(SellerBalanceLogFormat.START, schedulerName);

        try {
            dailySellerBalanceService.processDailySettlement();
            log.info(SellerBalanceLogFormat.COMPLETE, schedulerName);
        } catch (Exception e) {
            log.error(SellerBalanceLogFormat.FAIL, schedulerName);
        }
    }
}
