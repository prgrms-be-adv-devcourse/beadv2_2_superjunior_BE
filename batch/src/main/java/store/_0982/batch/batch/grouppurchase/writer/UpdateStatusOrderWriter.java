package store._0982.batch.batch.grouppurchase.writer;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.order.Order;
import store._0982.batch.domain.order.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateStatusOrderWriter implements ItemWriter<Order> {

    private final OrderRepository orderRepository;

    @Override
    public void write(Chunk<? extends Order> chunk){
        List<Order> orderList = new ArrayList<>(chunk.getItems());
        orderRepository.saveAll(orderList);
    }
}
