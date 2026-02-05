package store._0982.member.application.member.dto;

import store._0982.common.auth.Role;

import java.util.UUID;

public record RoleInfo (
    UUID memberId,
    Role role
){
}
