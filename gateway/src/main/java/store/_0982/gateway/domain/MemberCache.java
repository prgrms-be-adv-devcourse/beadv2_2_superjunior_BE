package store._0982.gateway.domain;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface MemberCache {
    Mono<Member> findById(UUID id);
}
