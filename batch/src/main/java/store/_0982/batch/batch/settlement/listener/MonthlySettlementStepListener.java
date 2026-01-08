package store._0982.batch.batch.settlement.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class MonthlySettlementStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("[STEP LISTENER] Step 시작: " + stepExecution.getStepName());
        System.out.println("[STEP LISTENER] JobExecutionId: " + stepExecution.getJobExecutionId());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("[STEP LISTENER] Step 완료: " + stepExecution.getStepName());
        System.out.println("[STEP LISTENER] Status: " + stepExecution.getStatus());
        return stepExecution.getExitStatus();
    }
}
