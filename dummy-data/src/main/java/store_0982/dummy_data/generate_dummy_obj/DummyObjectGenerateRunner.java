package store_0982.dummy_data.generate_dummy_obj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyGroupPurchaseGenerator;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyOrderGenerator;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyCanceledOrderGenerator;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyOrderSettlementGenerator;
import store_0982.dummy_data.generate_dummy_obj.commerce.DummyProductGenerator;
import store_0982.dummy_data.generate_dummy_obj.member.DummyMemberGenerator;
import store_0982.dummy_data.generate_dummy_obj.member.DummySellerGenerator;
import store_0982.dummy_data.generate_dummy_obj.recommendation.DummyProductVectorGenerator;

@Component
@Slf4j
@RequiredArgsConstructor
@Order(2)
public class DummyObjectGenerateRunner implements ApplicationRunner {

    private final DummyProductGenerator dummyProductGenerator;
    private final DummyProductVectorGenerator dummyProductVectorGenerator;
    private final DummyMemberGenerator dummyMemberGenerator;
    private final DummySellerGenerator dummySellerGenerator;
    private final DummyGroupPurchaseGenerator dummyGroupPurchaseGenerator;
    private final DummyOrderGenerator dummyOrderGenerator;
    private final DummyCanceledOrderGenerator dummyCanceledOrderGenerator;
    private final DummyOrderSettlementGenerator dummyOrderSettlementGenerator;

    @Value("${dummy-data.product-id-pool.count}")
    private int productCount;
    @Value("${dummy-data.group-purchase-id-pool.count}")
    private int groupPurchaseCount;
    @Value("${dummy-data.order-id-pool.count}")
    private int orderCount;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        log.info("[더미데이터] 상품 더미 생성 시작. count={}", productCount);
        dummyProductGenerator.generateAndWriteCsv(productCount);
        log.info("[더미데이터] 상품 더미 생성 완료.");

        log.info("[더미데이터] 상품 벡터 더미 생성 시작.");
        dummyProductVectorGenerator.readIdAndWriteProductVector();
        log.info("[더미데이터] 상품 벡터 더미 생성 완료.");

        log.info("[더미데이터] 공동구매 더미 생성 시작. count={}", groupPurchaseCount);
        dummyGroupPurchaseGenerator.generateAndWriteCsv(groupPurchaseCount);
        log.info("[더미데이터] 공동구매 더미 생성 완료.");

        log.info("[더미데이터] 회원 더미 생성 시작.");
        dummyMemberGenerator.readIdAndWriteMember();
        log.info("[더미데이터] 회원 더미 생성 완료.");

        log.info("[더미데이터] 판매자 더미 생성 시작.");
        dummySellerGenerator.readIdAndWriteSeller();
        log.info("[더미데이터] 판매자 더미 생성 완료.");

        log.info("[더미데이터] 주문 더미 생성 시작. count={}", orderCount);
        dummyOrderGenerator.generateAndWriteCsv(orderCount);
        log.info("[더미데이터] 주문 더미 생성 완료.");

        log.info("[더미데이터] 취소 주문 더미 생성 시작.");
        dummyCanceledOrderGenerator.generate();
        log.info("[더미데이터] 취소 주문 더미 생성 완료.");

        log.info("[더미데이터] 정산 더미 생성 시작.");
        dummyOrderSettlementGenerator.generate();
        log.info("[더미데이터] 정산 더미 생성 완료.");

        log.info("더미데이터 생성 완료");
    }
}
