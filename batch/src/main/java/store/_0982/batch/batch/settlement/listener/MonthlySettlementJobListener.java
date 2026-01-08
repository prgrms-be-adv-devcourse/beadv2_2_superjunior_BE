package store._0982.batch.batch.settlement.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class MonthlySettlementJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("[JOB LISTENER] Job 시작: " + jobExecution.getJobInstance().getJobName());
        System.out.println("[JOB LISTENER] JobExecutionId: " + jobExecution.getJobId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("[JOB LISTENER] Job 완료: " + jobExecution.getJobInstance().getJobName());
        System.out.println("[JOB LISTENER] Status: " + jobExecution.getStatus());
    }
}
