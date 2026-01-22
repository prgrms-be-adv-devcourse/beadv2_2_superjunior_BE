package store._0982.batch.batch.sellerbalance.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.sellerbalance.*;
import store._0982.batch.domain.settlement.OrderSettlement;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerBalanceWriter implements ItemWriter<OrderSettlement> {


    @Override
    public void write(Chunk<? extends OrderSettlement> chunk) {
        List<OrderSettlement> orderSettlements = chunk.getItems().stream()
                .map(gp -> (OrderSettlement) gp)
                .toList();

        if (orderSettlements.isEmpty()) {
            return;
        }


    }
}
