package store._0982.order.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MonthlySettlementScheduler {

    // private final MonthlySettlementService monthlySettlementService;

    /**
     * 매월 1일 02:00에 데일리 정산 실행
     */
    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Seoul")
    public void scheduleMonthlySettlement() {
        try {
           // monthlySettlementService.processMonthlySettlement();
        } catch (Exception e) {
            log.error("먼슬리 정산 스케줄러 실패", e);
        }
    }
}
