package store._0982.member.application.dto;

import store._0982.member.domain.Role;

import java.util.UUID;

public record SellerRegisterCommand(UUID memberId, Role role, String accountNumber, String bankCode,
                                    String accountHolder, String businessRegistrationNumber) {
}
