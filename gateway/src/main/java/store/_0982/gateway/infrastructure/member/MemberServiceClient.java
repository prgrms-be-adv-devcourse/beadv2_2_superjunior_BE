package store._0982.gateway.infrastructure.member;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import store._0982.gateway.domain.Member;
import store._0982.gateway.domain.Role;

import java.util.UUID;

/**
 * member-service 로부터 역할 정보를 조회하는 클라이언트.
 */
@Component
@RequiredArgsConstructor
public class MemberServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${member.service.url}")
    private String memberServiceBaseUrl;

    public Mono<Member> fetchMember(UUID memberId) {
        return webClientBuilder
                .baseUrl(memberServiceBaseUrl)
                .build()
                .get()
                .uri("/api/members/role")
                .header("X-Member-Id", memberId.toString())
                .retrieve()
                .bodyToMono(RoleResponse.class)
                .flatMap(response -> {
                    Role role = toRole(response);
                    if (role == null) {
                        return Mono.empty();
                    }
                    return Mono.just(Member.of(memberId, role));
                });
    }

    private Role toRole(RoleResponse response) {
        if (response == null || response.data() == null || response.data().role() == null) {
            return null;
        }
        try {
            return Role.valueOf(response.data().role());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private record RoleResponse(RoleData data) {
    }

    private record RoleData(String role) {
    }
}
