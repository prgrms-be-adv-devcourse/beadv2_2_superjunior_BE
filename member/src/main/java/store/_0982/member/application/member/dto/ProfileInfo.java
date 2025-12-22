package store._0982.member.application.member.dto;

import store._0982.member.domain.member.Member;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileInfo(
        UUID memberId,
        String email,
        String name,
        OffsetDateTime createdAt,
        String role,
        String imageUrl,
        String phoneNumber
) {
    public static ProfileInfo from(Member member) {
        return new ProfileInfo(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getCreatedAt(),
                member.getRole().name(),
                member.getImageUrl(),
                member.getPhoneNumber()
        );
    }
}
