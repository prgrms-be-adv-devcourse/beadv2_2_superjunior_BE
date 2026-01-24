package store._0982.batch.batch.sellerbalance.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.common.log.BatchLogMessageFormat;
import store._0982.common.log.BatchLogMetadataFormat;

/**
 * 일간 정산 Writer 에러 처리
 */
@Slf4j
@StepScope
@Component
public class SellerBalanceWriterListener implements ItemWriteListener<SellerBalance> {

    private final StepExecution stepExecution;

    public SellerBalanceWriterListener(
            @Value("#{stepExecution}") StepExecution stepExecution
    ){
        this.stepExecution = stepExecution;
    }

    @Override
    public void onWriteError(Exception ex, Chunk<? extends SellerBalance> items) {
        String jobName = stepExecution.getJobExecution().getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();

        log.error(
                BatchLogMessageFormat.itemReaderFailed(jobName, stepName),
                BatchLogMetadataFormat.itemReaderFailed(
                        jobName,
                        stepName,
                        "sellerBalanceWriter",
                        ex.getClass().getSimpleName(),
                        ex.getMessage()
                )
        );
    }
}
