package store._0982.member.infrastructure.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@FeignClient(
        name = "point-service", //todo: 엔드포인트 완성 시 연결 필요
        url = "http://localhost:8081"
)
public interface PointFeignClient{      //todo: 엔드포인트 재작성 필요
    @PostMapping("/api/point/{id}")
    ResponseEntity<Void> postPointBalance(@PathVariable("id") UUID id);

    @DeleteMapping("/api/point/{id}")
    ResponseEntity<Void> deletePointBalance(@PathVariable("id") UUID id);
}
