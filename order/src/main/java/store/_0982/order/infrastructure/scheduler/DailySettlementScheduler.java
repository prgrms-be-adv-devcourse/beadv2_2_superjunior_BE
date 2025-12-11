package store._0982.order.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store._0982.order.application.settlement.DailySettlementService;

@Slf4j
@RequiredArgsConstructor
@Component
public class DailySettlementScheduler {

    private final DailySettlementService dailySettlementService;

    private static final String SCHEDULER_START = "[SCHEDULER] [%s] started";
    private static final String SCHEDULER_COMPLETE = "[SCHEDULER] [%s] completed";
    private static final String SCHEDULER_FAIL = "[SCHEDULER] [%s] failed";

    /**
     * 매일 01:00에 데일리 정산 실행
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    public void scheduleDailySettlement() {
        String schedulerName = "DailySettlement";
        log.info(String.format(SCHEDULER_START, schedulerName));

        try {
            dailySettlementService.processDailySettlement();
            log.info(String.format(SCHEDULER_COMPLETE, schedulerName));
        } catch (Exception e) {
            log.error(String.format(SCHEDULER_FAIL, schedulerName));
        }
    }
}
