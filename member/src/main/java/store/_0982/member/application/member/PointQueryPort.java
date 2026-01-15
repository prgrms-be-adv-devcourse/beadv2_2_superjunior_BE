package store._0982.member.application.member;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

public interface PointQueryPort {
    ResponseEntity<Void> postPointBalance(@PathVariable("id") UUID id);

    ResponseEntity<Void> deletePointBalance(@PathVariable("id") UUID id);
}
