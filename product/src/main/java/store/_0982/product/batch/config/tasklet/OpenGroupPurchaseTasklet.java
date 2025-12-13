package store._0982.product.batch.config.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import store._0982.product.domain.GroupPurchaseRepository;

import java.time.OffsetDateTime;

@Slf4j
@RequiredArgsConstructor
public class OpenGroupPurchaseTasklet implements Tasklet {
    private final GroupPurchaseRepository groupPurchaseRepository;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        OffsetDateTime now = OffsetDateTime.now();

        // SCHEDULED 이면서 시작 시간 지난 공동구매 찾기
        int updatedCount = groupPurchaseRepository.openReadyGroupPurchases(now);

        log.info("공동 구매 오픈 완료: {}건", updatedCount);

        contribution.incrementWriteCount(updatedCount);

        return RepeatStatus.FINISHED;
    }
}
