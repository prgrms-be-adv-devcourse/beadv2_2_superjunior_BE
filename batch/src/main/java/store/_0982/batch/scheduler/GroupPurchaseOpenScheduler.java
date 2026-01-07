package store._0982.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GroupPurchaseOpenScheduler {
    private final JobLauncher jobLauncher;
    private final Job openGroupPurchaseJob;

    // @Scheduled(cron = "0 0 * * * *")
    public void runOpenJob(){
        try{
            log.info("공동구매 오픈 배치 시작");

            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(openGroupPurchaseJob, params);

            log.info("공동 구매 오픈 배치 완료");
        }catch(Exception e){
            log.error("공동 구매 오픈 배치 실패" , e);
        }
    }
}
