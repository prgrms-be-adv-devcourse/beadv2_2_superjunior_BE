package store._0982.batch.batch.grouppurchase.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.batch.grouppurchase.event.OrderUpdatedEvent;
import store._0982.batch.domain.order.Order;
import store._0982.batch.domain.order.OrderRepository;
import store._0982.batch.domain.order.OrderStatus;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateStatusOrderWriter implements ItemWriter<Order> {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends Order> chunk){
        List<Order> orderList = new ArrayList<>(chunk.getItems());
        orderRepository.saveAll(orderList);

        orderList.stream()
                .filter(order -> order.getStatus() == OrderStatus.GROUP_PURCHASE_FAIL)
                .forEach(order -> eventPublisher.publishEvent(
                        new OrderUpdatedEvent(order.getOrderId())
                ));
    }
}
