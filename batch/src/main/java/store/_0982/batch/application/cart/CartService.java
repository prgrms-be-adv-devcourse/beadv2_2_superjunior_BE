package store._0982.batch.application.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.cart.CartRepository;
import store._0982.common.log.ServiceLog;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {
    private final CartRepository cartRepository;

    @Transactional
    @ServiceLog
    public void cleanUpZeroCarts() {
        cartRepository.deleteAllZeroQuantity();
    }

}
