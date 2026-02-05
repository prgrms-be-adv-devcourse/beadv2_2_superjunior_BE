package store_0982.dummy_data.generate_dummy_obj.member.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import store._0982.common.auth.Role;
import store._0982.member.domain.member.Member;

public record MemberRowCsv(
        OffsetDateTime createdAt,
        OffsetDateTime deletedAt,
        String email,
        String imageUrl,
        UUID memberId,
        String name,
        String password,
        String phoneNumber,
        Role role,
        String saltKey,
        Member.Status status,
        OffsetDateTime updatedAt
) {
    public static MemberRowCsv from(Member member) {
        return new MemberRowCsv(
                member.getCreatedAt(),
                member.getDeletedAt(),
                member.getEmail(),
                member.getImageUrl(),
                member.getMemberId(),
                member.getName(),
                member.getPassword(),
                member.getPhoneNumber(),
                member.getRole(),
                member.getSaltKey(),
                member.getStatus(),
                member.getUpdatedAt()
        );
    }

}
