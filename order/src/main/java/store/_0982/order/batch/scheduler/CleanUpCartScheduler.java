package store._0982.order.batch.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store._0982.order.application.cart.CartService;

@Component
@RequiredArgsConstructor
public class CleanUpCartScheduler {
    private final CartService cartService;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanUpCarts(){
        cartService.cleanUpZeroCarts();
    }
}
