package store._0982.batch.batch.settlement.config;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

/**
 * 타임스탬프 기반 JobParametersIncrementer
 * K8s CronJob 환경에서 매 실행마다 자동으로 유니크한 파라미터 생성
 */
public class TimestampIncrementer implements JobParametersIncrementer {

    @Override
    public JobParameters getNext(JobParameters parameters) {
        return new JobParametersBuilder(parameters)
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();
    }
}
