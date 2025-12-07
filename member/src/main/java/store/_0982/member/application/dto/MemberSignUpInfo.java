package store._0982.member.application.dto;

import store._0982.member.domain.Member;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MemberSignUpInfo (
    UUID memberId,
    String email,
    String name,
    String phoneNumber,
    OffsetDateTime createdAt
){
    public static MemberSignUpInfo from(Member member) {
        return new MemberSignUpInfo (member.getMemberId(), member.getEmail(), member.getName(), member.getPhoneNumber(), member.getCreatedAt());
    }
}
