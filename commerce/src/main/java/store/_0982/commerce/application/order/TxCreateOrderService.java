package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.order.dto.OrderRegisterCommand;
import store._0982.commerce.application.order.dto.OrderRegisterInfo;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.order.Order;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TxCreateOrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public OrderRegisterInfo create(UUID memberId, OrderRegisterCommand command, GroupPurchase groupPurchase){
        Order order = Order.create(
                command.quantity(),
                groupPurchase.getDiscountedPrice(),
                ((long) command.quantity() * groupPurchase.getDiscountedPrice()),
                memberId,
                command.address(),
                command.addressDetail(),
                command.postalCode(),
                command.receiverName(),
                command.sellerId(),
                groupPurchase.getGroupPurchaseId(),
                command.requestId()
        );

        Order savedOrder = orderRepository.save(order);
        return OrderRegisterInfo.from(savedOrder);
    }
}
