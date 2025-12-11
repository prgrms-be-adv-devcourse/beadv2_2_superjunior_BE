package store._0982.order.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.order.application.dto.OrderRegisterCommand;
import store._0982.order.application.dto.OrderRegisterInfo;
import store._0982.order.domain.OrderRepository;
import store._0982.order.domain.Order;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    /**
     * 주문 생성
     * @param memberId 고객
     * @param command 주문 command
     * @return OrderRegisterInfo
     */
    public OrderRegisterInfo createOrder(UUID memberId, OrderRegisterCommand command) {
        // TODO: Feign으로 sellerId, groupPurchaseId 존재하는지 확인 필요

        Order order = new Order(
               command.quantity(),
               command.price(),
               memberId,
               command.address(),
               command.addressDetail(),
               command.postalCode(),
               command.receiverName(),
               command.sellerId(),
               command.groupPurchaseId()
        );
        Order savedOrder = orderRepository.save(order);
        return OrderRegisterInfo.from(savedOrder);
    }
}
