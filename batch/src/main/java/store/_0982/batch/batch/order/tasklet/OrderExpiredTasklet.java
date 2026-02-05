package store._0982.batch.batch.order.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.order.OrderRepository;
import store._0982.common.domain.order.Order;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderExpiredTasklet implements Tasklet {

    private final OrderRepository orderRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext){
        OffsetDateTime now =  OffsetDateTime.now();

        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(now);

        for(Order order : expiredOrders){
            order.markExpired();
        }

        orderRepository.saveAll(expiredOrders);

        contribution.incrementWriteCount(expiredOrders.size());

        return RepeatStatus.FINISHED;
    }
}
