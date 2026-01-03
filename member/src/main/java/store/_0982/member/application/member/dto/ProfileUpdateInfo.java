package store._0982.member.application.member.dto;

import store._0982.member.domain.member.Member;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProfileUpdateInfo(
        UUID memberId,
        String email,
        String name,
        String phoneNumber,
        OffsetDateTime updatedAt
) {
    public static ProfileUpdateInfo from(Member member) {
        return new ProfileUpdateInfo(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNumber(),
                member.getUpdatedAt()
        );
    }
}
