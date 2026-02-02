package store._0982.point.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import store._0982.common.exception.CustomException;
import store._0982.point.client.dto.TossPaymentInfo;
import store._0982.point.domain.constant.PaymentMethod;
import store._0982.point.domain.vo.PaymentMethodDetail;
import store._0982.point.exception.CustomErrorCode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentMethodDetailMapper {

    public static PaymentMethodDetail from(TossPaymentInfo tossPaymentInfo) {
        PaymentMethod method = tossPaymentInfo.paymentMethod();

        return switch (method) {
            case CARD -> PaymentMethodDetail.card(tossPaymentInfo.card());
            case VIRTUAL_ACCOUNT -> PaymentMethodDetail.virtualAccount(tossPaymentInfo.virtualAccount());
            case EASY_PAY -> PaymentMethodDetail.easyPay(tossPaymentInfo.easyPay());
            case MOBILE -> PaymentMethodDetail.mobilePay(tossPaymentInfo.mobilePay());
            case TRANSFER -> PaymentMethodDetail.transfer(tossPaymentInfo.transfer());
            case GIFT -> PaymentMethodDetail.gift(tossPaymentInfo.gift());
            case UNKNOWN -> throw new CustomException(CustomErrorCode.FAILED_TO_MAP_PAYMENT_METHOD);
        };
    }
}
