package store._0982.batch.batch.settlement.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.common.log.BatchLogMessageFormat;
import store._0982.common.log.BatchLogMetadataFormat;

/**
 * LowBalance Reader 에러 처리
 */
@Slf4j
@StepScope
@Component
public class LowBalanceItemReaderListener implements ItemReadListener<SellerBalance> {

    private final StepExecution stepExecution;

    public LowBalanceItemReaderListener(
            @Value("#{stepExecution}") StepExecution stepExecution
    ) {
        this.stepExecution = stepExecution;
    }

    @Override
    public void onReadError(Exception ex) {
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();


        log.error(
                BatchLogMessageFormat.itemReaderFailed(jobName, stepName),
                BatchLogMetadataFormat.itemReaderFailed(
                        jobName,
                        stepName,
                        "lowBalanceReader",
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                )
        );
    }
}
