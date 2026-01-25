package store._0982.point.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.payment.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record TossPaymentInfo(
        String version,
        String paymentKey,
        String lastTransactionKey,
        String secret,      // 웹훅 검증용 secret (nullable)

        UUID orderId,
        @JsonProperty("totalAmount") long amount,
        String method,
        Status status,

        Card card,
        VirtualAccount virtualAccount,
        EasyPay easyPay,
        Transfer transfer,
        @JsonProperty("mobilePhone") MobilePay mobilePay,
        @JsonProperty("giftCertificate") Gift gift,

        OffsetDateTime requestedAt,
        OffsetDateTime approvedAt,

        List<CancelInfo> cancels,
        FailureInfo failure
) implements PaymentInfo {

    @Override
    public PaymentMethod paymentMethod() {
        return switch (this.method) {
            case "카드" -> PaymentMethod.CARD;
            case "가상계좌" -> PaymentMethod.VIRTUAL_ACCOUNT;
            case "간편결제" -> PaymentMethod.EASY_PAY;
            case "휴대폰" -> PaymentMethod.MOBILE;
            case "계좌이체" -> PaymentMethod.TRANSFER;
            case "문화상품권", "도서문화상품권", "게임문화상품권" -> PaymentMethod.GIFT;
            default -> PaymentMethod.UNKNOWN;
        };
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(
            long amount,
            String issuerCode,
            String number,
            int installmentPlanMonths,
            String approveNo,
            String cardType,
            String ownerType
    ) implements CardInfo {
        @Override
        public String cardNumber() {
            return number;
        }

        @Override
        public int installmentMonths() {
            return installmentPlanMonths;
        }

        @Override
        public String approveNumber() {
            return approveNo;
        }
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record VirtualAccount(
            String accountType,
            String accountNumber,
            String bankCode,
            String customerName,
            String depositorName,
            boolean expired
    ) implements VirtualAccountInfo {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EasyPay(
            String provider,
            long amount,
            long discountAmount
    ) implements EasyPayInfo {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Transfer(
            String bankCode
    ) implements TransferInfo {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Gift(
            String approveNo
    ) implements GiftInfo {
        @Override
        public String approveNumber() {
            return approveNo;
        }
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MobilePay(
            String customerMobilePhone,
            String receiptUrl
    ) implements MobilePayInfo {
        @Override
        public String phoneNumber() {
            return customerMobilePhone;
        }
    }

    public enum Status {
        READY,
        IN_PROGRESS,
        WAITING_FOR_DEPOSIT,
        DONE,
        CANCELED,
        PARTIAL_CANCELED,
        ABORTED,
        EXPIRED
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CancelInfo(
            long cancelAmount,
            String cancelReason,
            OffsetDateTime canceledAt,
            String transactionKey
    ) {
    }

    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FailureInfo(
            String code,
            String message
    ) {
    }
}
