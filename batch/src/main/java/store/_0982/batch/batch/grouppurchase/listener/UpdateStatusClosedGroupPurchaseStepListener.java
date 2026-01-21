package store._0982.batch.batch.grouppurchase.listener;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.processor.UpdateStatusClosedGroupPurchaseProcessor;
import store._0982.batch.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.batch.domain.product.Product;
import store._0982.batch.domain.product.ProductRepository;
import store._0982.common.log.BatchLogMessageFormat;
import store._0982.common.log.BatchLogMetadataFormat;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
@Slf4j
public class UpdateStatusClosedGroupPurchaseStepListener implements StepExecutionListener {

    private final EntityManager entityManager;
    private final ProductRepository productRepository;
    private final UpdateStatusClosedGroupPurchaseProcessor updateStatusClosedGroupPurchaseProcessor;

    /**
     * 청크 처리 전에, 청크의 GroupPurchase가 참조하는 Product 미리 로드
     */
    @Override
    public void beforeStep(@Nonnull StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        String jobName = jobExecution.getJobInstance().getJobName();
        String stepName = stepExecution.getStepName();
        Long jobExecutionId = stepExecution.getJobExecutionId();

        log.info(
                BatchLogMessageFormat.stepStart(jobName, stepName),
                BatchLogMetadataFormat.stepStart(
                        jobName,
                        stepName,
                        jobExecutionId
                )
        );

        String nowStr = stepExecution.getJobParameters().getString("now");
        if (nowStr == null) {
            log.warn("'now' job parameter missing.");
            updateStatusClosedGroupPurchaseProcessor.setProductMap(Collections.emptyMap());
            return;
        }

        OffsetDateTime now = OffsetDateTime.parse(nowStr);

        // Reader에서 가져온 GroupPurchase와 같은 조건으로 Product 조회
        List<UUID> productIds = entityManager.createQuery(
                        "SELECT g.productId FROM GroupPurchase g " +
                                "WHERE g.status = :status " +
                                "AND g.endDate <= :now", UUID.class)
                .setParameter("status", GroupPurchaseStatus.OPEN)
                .setParameter("now", now)
                .getResultList();
        if (productIds.isEmpty()) {
            updateStatusClosedGroupPurchaseProcessor.setProductMap(Collections.emptyMap());
            return;
        }

        List<Product> products = productRepository.findAllByIdIn(productIds);

        Map<UUID, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getProductId, Function.identity()));

        updateStatusClosedGroupPurchaseProcessor.setProductMap(productMap);

    }

    @Override
    public ExitStatus afterStep(@Nonnull StepExecution stepExecution) {
        updateStatusClosedGroupPurchaseProcessor.setProductMap(null);
        return stepExecution.getExitStatus();
    }
}
