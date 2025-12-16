package store._0982.order.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store._0982.order.application.settlement.DailySettlementService;
import store._0982.order.infrastructure.settlement.SettlementLogFormat;

@Slf4j
@RequiredArgsConstructor
@Component
public class DailySettlementScheduler {

    private final DailySettlementService dailySettlementService;

    /**
     * 매일 01:00에 데일리 정산 실행
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void scheduleDailySettlement() {
        String schedulerName = "DailySettlement";
        log.info(SettlementLogFormat.START, schedulerName);

        try {
            dailySettlementService.processDailySettlement();
            log.info(SettlementLogFormat.COMPLETE, schedulerName);
        } catch (Exception e) {
            log.error(SettlementLogFormat.FAIL, schedulerName);
        }
    }
}
