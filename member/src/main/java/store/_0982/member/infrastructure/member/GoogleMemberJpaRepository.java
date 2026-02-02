package store._0982.member.infrastructure.member;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.member.domain.member.GoogleMember;

public interface GoogleMemberJpaRepository extends JpaRepository<GoogleMember, String> {
}
