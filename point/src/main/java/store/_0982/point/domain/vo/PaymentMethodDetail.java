package store._0982.point.domain.vo;

import jakarta.persistence.Embeddable;
import lombok.*;
import store._0982.point.domain.payment.*;

@Getter
@Embeddable
@Builder(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentMethodDetail {

    // ========= 카드 결제 =========
    private String cardNumber;

    private String issuerCode;              // 카드 발급사 코드

    private String approveNumber;           // 상품권 결제에도 이용됨

    private Integer installmentPlanMonths;  // 할부 개월 수

    // ========= 가상 계좌 결제 =========
    private String accountNumber;

    private String bankCode;                // 계좌 이체에도 이용됨

    private String customerName;

    private String depositorName;

    // ========= 모바일 결제 =========
    private String phoneNumber;

    // ========= 간편 결제 =========
    private String provider;

    public static PaymentMethodDetail card(CardInfo cardInfo) {
        return PaymentMethodDetail.builder()
                .cardNumber(cardInfo.cardNumber())
                .issuerCode(cardInfo.issuerCode())
                .approveNumber(cardInfo.approveNumber())
                .installmentPlanMonths(cardInfo.installmentMonths())
                .build();
    }

    public static PaymentMethodDetail virtualAccount(VirtualAccountInfo virtualAccountInfo) {
        return PaymentMethodDetail.builder()
                .accountNumber(virtualAccountInfo.accountNumber())
                .bankCode(virtualAccountInfo.bankCode())
                .customerName(virtualAccountInfo.customerName())
                .depositorName(virtualAccountInfo.depositorName())
                .build();
    }

    public static PaymentMethodDetail mobilePay(MobilePayInfo mobilePayInfo) {
        return PaymentMethodDetail.builder()
                .phoneNumber(mobilePayInfo.phoneNumber())
                .build();
    }

    public static PaymentMethodDetail easyPay(EasyPayInfo easyPayInfo) {
        return PaymentMethodDetail.builder()
                .provider(easyPayInfo.provider())
                .build();
    }

    public static PaymentMethodDetail transfer(TransferInfo transferInfo) {
        return PaymentMethodDetail.builder()
                .bankCode(transferInfo.bankCode())
                .build();
    }

    public static PaymentMethodDetail gift(GiftInfo giftInfo) {
        return PaymentMethodDetail.builder()
                .approveNumber(giftInfo.approveNumber())
                .build();
    }
}
