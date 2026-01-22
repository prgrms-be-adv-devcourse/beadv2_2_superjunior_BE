package store._0982.member.infrastructure.member.feign;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store._0982.common.dto.ResponseDto;
import store._0982.member.application.member.CommerceQueryPort;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class CommerceQueryFeignAdapter implements CommerceQueryPort {
    private final CommerceFeignClient commerceFeignClient;
    @Override
    @Retry(name = "CommerceClient")
    public ResponseDto<Void> postSellerBalance(UUID sellerId) {
        return commerceFeignClient.postSellerBalance(sellerId);
    }
}
