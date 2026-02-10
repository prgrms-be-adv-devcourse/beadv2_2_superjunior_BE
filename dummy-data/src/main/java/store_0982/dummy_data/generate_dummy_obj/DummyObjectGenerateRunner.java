package store_0982.dummy_data.generate_dummy_obj;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
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

    /*
     * 파라미터:
     *  - 기본: 전체 생성 (옵션 없음)
     *  - 선택: --generators=product,order
     *  - 전체: --generators=all
     *  - 빈 값: --generators=  (아무 것도 생성하지 않음)
     *  - 생성기 추가 : GeneratorType, parseTargets 수정
     */

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
    public void run(ApplicationArguments args) throws Exception {
        var targets = parseTargets(args);
        if (targets.isEmpty()) {
            log.info("더미데이터 생성 완료 (skip: no generators selected)");
            return;
        }

        if (targets.contains(GeneratorType.PRODUCT)) {
            log.info("[더미데이터 상품 데이터 생성 시작. count={}]", productCount);
            dummyProductGenerator.generateAndWriteCsv(productCount);
            log.info("[더미데이터 상품 데이터 생성 완료.]");
        }

        if (targets.contains(GeneratorType.PRODUCT_VECTOR)) {
            log.info("[더미데이터 상품 벡터 데이터 생성 시작.]");
            dummyProductVectorGenerator.readIdAndWriteProductVector();
            log.info("[더미데이터 상품 벡터 데이터 생성 완료.]");
        }

        if (targets.contains(GeneratorType.GROUP_PURCHASE)) {
            log.info("[더미데이터 공동구매 데이터 생성 시작. count={}]", groupPurchaseCount);
            dummyGroupPurchaseGenerator.generateAndWriteCsv(groupPurchaseCount);
            log.info("[더미데이터 공동구매 데이터 생성 완료.]");
        }

        if (targets.contains(GeneratorType.MEMBER)) {
            log.info("[더미데이터 회원 데이터 생성 시작.]");
            dummyMemberGenerator.readIdAndWriteMember();
            log.info("[더미데이터 회원 데이터 생성 완료.]");
        }

        if (targets.contains(GeneratorType.SELLER)) {
            log.info("[더미데이터 판매자 데이터 생성 시작.]");
            dummySellerGenerator.readIdAndWriteSeller();
            log.info("[더미데이터 판매자 데이터 생성 완료.]");
        }

        if (targets.contains(GeneratorType.ORDER)) {
            log.info("[더미데이터 주문 데이터 생성 시작. count={}]", orderCount);
            dummyOrderGenerator.generateAndWriteCsv(orderCount);
            log.info("[더미데이터 주문 데이터 생성 완료.]");
        }

        if (targets.contains(GeneratorType.CANCELED_ORDER)) {
            log.info("[더미데이터 취소 주문 더미 생성 시작.]");
            dummyCanceledOrderGenerator.generate();
            log.info("[더미데이터 취소 주문 더미 생성 완료.]");
        }

        if (targets.contains(GeneratorType.ORDER_SETTLEMENT)) {
            log.info("[더미데이터 정산 더미 생성 시작.]");
            dummyOrderSettlementGenerator.generate();
            log.info("[더미데이터 정산 더미 생성 완료.]");
        }

        log.info("더미데이터 생성 완료");
    }

    private enum GeneratorType {
        PRODUCT,
        PRODUCT_VECTOR,
        GROUP_PURCHASE,
        MEMBER,
        SELLER,
        ORDER,
        CANCELED_ORDER,
        ORDER_SETTLEMENT
    }

    private java.util.EnumSet<GeneratorType> parseTargets(ApplicationArguments args) {
        if (!args.containsOption("generators")) {
            return java.util.EnumSet.allOf(GeneratorType.class);
        }

        var values = args.getOptionValues("generators");
        if (values == null || values.isEmpty()) {
            return java.util.EnumSet.noneOf(GeneratorType.class);
        }

        var raw = String.join(",", values).trim();
        if (raw.isEmpty()) {
            return java.util.EnumSet.noneOf(GeneratorType.class);
        }

        if ("all".equalsIgnoreCase(raw)) {
            return java.util.EnumSet.allOf(GeneratorType.class);
        }

        var set = java.util.EnumSet.noneOf(GeneratorType.class);
        for (String token : raw.split(",")) {
            String key = token.trim().toLowerCase();
            switch (key) {
                case "product" -> set.add(GeneratorType.PRODUCT);
                case "product-vector", "product_vector", "vector" -> set.add(GeneratorType.PRODUCT_VECTOR);
                case "group-purchase", "group_purchase", "grouppurchase" -> set.add(GeneratorType.GROUP_PURCHASE);
                case "member" -> set.add(GeneratorType.MEMBER);
                case "seller" -> set.add(GeneratorType.SELLER);
                case "order" -> set.add(GeneratorType.ORDER);
                case "canceled-order" -> set.add(GeneratorType.CANCELED_ORDER);
                case "order-settlement" -> set.add(GeneratorType.ORDER_SETTLEMENT);
                default -> log.warn("Unknown generator target: {}", token);
            }
        }
        return set;
    }
}
