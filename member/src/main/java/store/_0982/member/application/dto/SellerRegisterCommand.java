package store._0982.member.application.dto;

import java.util.UUID;

public record SellerRegisterCommand(UUID memberId, String accountNumber, String bankCode, String accountHolder,
                                    String businessRegistrationNumber) {
}
