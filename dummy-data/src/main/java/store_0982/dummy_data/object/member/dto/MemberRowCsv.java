package store_0982.dummy_data.object.member.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.auth.Role;
import store._0982.member.domain.member.Member;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({
        "memberId",
        "email",
        "name",
        "password",
        "phoneNumber",
        "role",
        "saltKey",
        "imageUrl",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "status"
})
public record MemberRowCsv(
        UUID memberId,
        String email,
        String name,
        String password,
        String phoneNumber,
        Role role,
        String saltKey,
        String imageUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt,
        Member.Status status
) {
    public static MemberRowCsv from(Member member) {
        return new MemberRowCsv(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getPassword(),
                member.getPhoneNumber(),
                member.getRole(),
                member.getSaltKey(),
                member.getImageUrl(),
                member.getCreatedAt(),
                member.getUpdatedAt(),
                member.getDeletedAt(),
                member.getStatus()
        );
    }

}
