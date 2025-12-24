package store._0982.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupPurchaseCloseScheduler {
    private final JobLauncher jobLauncher;
    private final Job closeExpiredGroupPurchaseJob;

    @Scheduled(cron = "0 0 * * * *")
    public void runBatch(){
        try{
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("now",OffsetDateTime.now().toString())
                    .toJobParameters();

            log.info("공동구매 마감 배치 시작");

            jobLauncher.run(closeExpiredGroupPurchaseJob, params);

            log.info("공동구매 마감 배치 완료");
        } catch(Exception e){
            log.error("배치 실패",e);
        }
    }
}
