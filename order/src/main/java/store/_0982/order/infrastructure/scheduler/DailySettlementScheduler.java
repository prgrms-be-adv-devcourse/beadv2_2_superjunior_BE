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

    /**
     * 매일 01:00에 데일리 정산 실행
     */
    @Scheduled(cron = "0 8 23 * * *", zone = "Asia/Seoul")
    public void scheduleDailySettlement() {
        try {
            dailySettlementService.processDailySettlement();
        } catch (Exception e) {
            log.error("데일리 정산 스케줄러 실패", e);
        }
    }
}
