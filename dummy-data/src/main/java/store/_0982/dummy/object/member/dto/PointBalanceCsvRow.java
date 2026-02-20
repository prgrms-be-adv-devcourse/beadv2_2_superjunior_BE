package store._0982.dummy.object.member.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.UUID;

@JsonPropertyOrder({
        "id",
        "member_id",
        "paid_point",
        "bonus_point",
        "last_used_at",
        "version"
})
public record PointBalanceCsvRow(
        UUID id,
        UUID memberId,
        long paidPoint,
        long bonusPoint,
        String lastUsedAt,
        long version
) {}
