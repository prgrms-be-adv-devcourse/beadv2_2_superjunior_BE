package store._0982.member.infrastructure.member.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import store._0982.common.HeaderName;

import java.util.UUID;

@FeignClient(
        name = "point-service",
        url = "http://localhost:8086"
)
public interface PointFeignClient{ 
    @PostMapping("/internal/points")
    ResponseEntity<Void> postPointBalance(@RequestHeader(value = HeaderName.ID) UUID memberId);
}
