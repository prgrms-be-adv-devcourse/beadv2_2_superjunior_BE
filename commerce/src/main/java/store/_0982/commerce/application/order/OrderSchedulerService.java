package store._0982.commerce.application.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.common.domain.order.Order;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSchedulerService {
    private final OrderRepository orderRepository;
    private final OrderExpirationService orderExpirationService;

    @ServiceLog
    public int processExpiredOrders(){
        OffsetDateTime now = OffsetDateTime.now();
        List<Order> expiredOrders = orderRepository.findExpiredPendingOrders(now);

        if(expiredOrders.isEmpty()){
            log.debug("만료된 주문 없음");
            return 0;
        }

        int successCount = 0;
        int failCount = 0;

        for(Order order : expiredOrders){
            try{
                orderExpirationService.expireSingleOrders(order.getOrderId());
                successCount++;
            }catch (CustomException e){
                failCount++;
                log.warn("주문 만료 처리 실패(Custom 에러) : orderId={}, error={}",order.getOrderId(), e.getMessage());
            }catch (Exception e){
                failCount++;
                log.error("주문 만료 처리 실패(시스템 에러) : orderId={}", order.getOrderId(), e);
            }
        }
        return successCount;
    }
}
