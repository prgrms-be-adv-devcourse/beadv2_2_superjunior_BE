package store._0982.member.presentation.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import store._0982.member.application.member.dto.SellerRegisterCommand;

import java.util.UUID;

public record SellerRegisterRequest(
    @NotBlank
    @Length(min = 1, max = 20)
    @Pattern(regexp = "^[0-9]+$", message = "계좌번호는 숫자만 입력 가능합니다.")
    String accountNumber,
    @NotBlank
    @Length(min = 1, max = 20)
    @Pattern(regexp = "^[0-9]+$", message = "은행 코드는 숫자만 입력 가능합니다.")
    String bankCode,
    @NotBlank
    @Length(min = 1, max = 50)
    @Pattern(regexp = "^[가-힣a-zA-Z\\s]+$", message = "예금주는 한글과 영어만 입력 가능합니다.")
    String accountHolder,
    @NotBlank
    @Length(min = 12, max = 12)
    @Pattern(
        regexp = "^[0-9]{3}-[0-9]{2}-[0-9]{5}$",
        message = "사업자 등록번호는 XXX-XX-XXXXX 형식으로 입력해야 합니다."
    )
    String businessRegistrationNumber
) {
    public SellerRegisterCommand toCommand(UUID memberId) {
        return new SellerRegisterCommand(memberId, accountNumber, bankCode, accountHolder, businessRegistrationNumber);
    }
}
