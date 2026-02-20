package store_0982.dummy_data.constants;

public final class DummyDataConstants {

    private DummyDataConstants() {}

    // 회원 역할 비율: 상위 20%는 SELLER, 나머지 80%는 CONSUMER
    public static final double SELLER_RATIO = 0.20;

    // 더미 회원 공통 비밀번호 (k6 스크립트에서 사용)
    public static final String DUMMY_PLAIN_PASSWORD = "dummy1234";

    // 모든 더미 회원의 고정 saltKey
    // 실제 인증: matches(saltKey + inputPassword, storedHash)
    public static final String DUMMY_SALT_KEY = "20240101000000";

    // 더미 이미지 URL
    public static final String DUMMY_IMAGE_URL = "https://dummy.local/image.png";

    // 상품 가격 범위
    public static final int PRODUCT_MIN_PRICE = 0;
    public static final int PRODUCT_MAX_PRICE = 1_000_000;

    // 상품 재고 범위
    public static final int PRODUCT_MIN_STOCK = 1;
    public static final int PRODUCT_MAX_STOCK = 10_000;

    // 상품 생성일 범위 (일)
    public static final int PRODUCT_CREATED_DAYS_RANGE = 365;
    public static final int PRODUCT_UPDATED_DAYS_RANGE = 30;

    // 공동구매 할인율 범위 (10~30%)
    public static final double GROUP_PURCHASE_MIN_DISCOUNT = 0.10;
    public static final double GROUP_PURCHASE_MAX_DISCOUNT = 0.30;

    // 주문당 수량 범위
    public static final int ORDER_MIN_QUANTITY = 1;
    public static final int ORDER_MAX_QUANTITY = 5;

    // 주문 취소 비율
    public static final double ORDER_CANCEL_RATIO = 0.20;

    // 주문 확정 비율 (취소 제외 후)
    public static final double ORDER_CONFIRM_RATIO = 0.80;

    // PointBalance 초기 포인트 잔액
    public static final long POINT_BALANCE_INITIAL = 0L;

    // SellerBalance 초기 정산 잔액
    public static final long SELLER_BALANCE_INITIAL = 0L;
}
