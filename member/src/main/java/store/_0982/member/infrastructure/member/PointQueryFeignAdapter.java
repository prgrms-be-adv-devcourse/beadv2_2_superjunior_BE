package store._0982.member.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import io.github.resilience4j.retry.annotation.Retry;
import store._0982.member.application.member.PointQueryPort;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PointQueryFeignAdapter implements PointQueryPort {

    private final PointFeignClient pointFeignClient;

    @Override
    @Retry(name = "pointClient")
    public ResponseEntity<Void> postPointBalance(UUID id) {
        return pointFeignClient.postPointBalance(id);
    }

    @Override
    @Retry(name = "pointClient")
    public ResponseEntity<Void> deletePointBalance(UUID id) {
        return pointFeignClient.deletePointBalance(id);
    }
}
