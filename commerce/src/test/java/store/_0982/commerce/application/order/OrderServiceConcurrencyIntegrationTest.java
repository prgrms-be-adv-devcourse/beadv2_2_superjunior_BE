package store._0982.commerce.application.order;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store._0982.commerce.application.grouppurchase.ParticipateService;
import store._0982.commerce.application.order.dto.OrderRegisterCommand;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.order.Order;
import store._0982.commerce.domain.order.OrderRepository;
import store._0982.commerce.domain.order.OrderStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductCategory;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.infrastructure.client.member.dto.ProfileInfo;
import store._0982.commerce.infrastructure.client.payment.PaymentClient;
import store._0982.commerce.infrastructure.order.OrderJpaRepository;
import store._0982.common.dto.ResponseDto;
import store._0982.common.exception.CustomException;
import store._0982.common.kafka.dto.GroupPurchaseChangedEvent;
import store._0982.common.kafka.dto.GroupPurchaseEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class OrderServiceConcurrencyIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ParticipateService participateConcurrencyService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private GroupPurchaseRepository groupPurchaseRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private MemberClient memberClient;

    @MockitoBean
    private PaymentClient paymentClient;


    @MockitoBean
    private KafkaTemplate<String, GroupPurchaseEvent> upsertKafkaTemplate;

    @MockitoBean
    private KafkaTemplate<String, GroupPurchaseChangedEvent> notificationKafkaTemplate;

    private ExecutorService executorService;

    private UUID testMemberId;
    private UUID testSellerId;
    private UUID testProductId;
    private GroupPurchase testGroupPurchase;

    @BeforeEach
    void setUp(){
        executorService = Executors.newFixedThreadPool(10);

        //testMemberId = UUID.randomUUID();
        testMemberId = UUID.fromString("3b73b8bf-3707-4c83-89a0-4315abbdb8b7");
        //testSellerId = UUID.randomUUID();
        testSellerId = UUID.fromString("3b73b8bf-3707-4c83-89a0-4315abbdb8b7");

        // Product 생성
        Product testProduct = createTestProduct();
        productRepository.save(testProduct);
        testProductId = testProduct.getProductId();

        // GroupPurchase 생성
        testGroupPurchase = createTestGroupPurchase();
        groupPurchaseRepository.save(testGroupPurchase);
        testGroupPurchase.updateStatus(GroupPurchaseStatus.OPEN);
        groupPurchaseRepository.save(testGroupPurchase);

        // MemberClient
        ProfileInfo profileInfo = new ProfileInfo(
                testMemberId,
                "test@example.com",
                "테스트유저",
                OffsetDateTime.now(),
                "CUSTOMER",
                null,
                "010-1234-5678"
        );
        ResponseDto<ProfileInfo> response = new ResponseDto<>(
                HttpStatus.OK,
                profileInfo,
                "조회 성공"
        );
        when(memberClient.getMember(testMemberId))
                .thenReturn(response);

    }

    @AfterEach
    void tearDown() {
        executorService.shutdown();
    }

    @Test
    @DisplayName("동시성 테스트 - 동일 공동구매에 여러 주문")
    void createOrder_concurrency_multipleOrders() throws InterruptedException{
        // given
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);

        when(paymentClient.deductPointsInternal(any(), any()))
                .thenReturn(new ResponseDto<>(200, null, "success"));

        when(memberClient.getMember(any()))
                .thenReturn(new ResponseDto<>(200, null, "success"));

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for(int i=0;i<threadCount;i++){
            executorService.submit(() -> {
                try{
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            2,
                            "서울시 강서구",
                            "101동 101호",
                            "12345",
                            "홍길동",
                            testMemberId,
                            testGroupPurchase.getGroupPurchaseId()
                    );

                    orderService.createOrder(testMemberId, command);
                    successCount.incrementAndGet();
                }catch (Exception e){
                    log.error("주문 실패" ,e);
                    failCount.incrementAndGet();
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);

        // then
        log.info("성공 : {}, 실패 :{}", successCount.get(), failCount.get());

        assertThat(successCount.get()).isEqualTo(threadCount);

        // DB에 저장
        List<Order> orders = orderRepository.findByGroupPurchaseIdAndDeletedAtIsNull(testGroupPurchase.getGroupPurchaseId());

        assertThat(orders).hasSize(threadCount);
    }

    @Test
    @DisplayName("동시성 테스트 - 최대 수량 초과 시 일부만 성공")
    void createOrder_concurrency_maxQuantityReached() throws InterruptedException{
        // given
        int maxQuantity = 100;
        int threadCount = 15;
        int orderQuantityPerThread = 10;

        CountDownLatch latch = new CountDownLatch(threadCount);

        when(memberClient.getMember(any()))
                .thenReturn(new ResponseDto<>(200, null, "success"));

        when(paymentClient.deductPointsInternal(any(), any()))
                .thenReturn(new ResponseDto<>(200, null, "success"));

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger lockFailCount = new AtomicInteger(0);

        for(int i=0;i<threadCount;i++){
            executorService.submit(() -> {
                try{
                    OrderRegisterCommand command = new OrderRegisterCommand(
                            2,
                            "서울시 강서구",
                            "101동 101호",
                            "12345",
                            "홍길동",
                            testMemberId,
                            testGroupPurchase.getGroupPurchaseId()
                    );
                    orderService.createOrder(testMemberId, command);
                    successCount.incrementAndGet();
                }catch (CustomException e){
                    if(e.getErrorCode() == CustomErrorCode.GROUP_PURCHASE_IS_REACHED){
                        log.info("수량 초과로 주문 실패");
                    }else {
                        log.error("예외",e);
                    }
                    failCount.incrementAndGet();
                }catch(ObjectOptimisticLockingFailureException e) {
                    log.info("낙관적 락 실패 - 동시성 충돌 발생");
                    lockFailCount.incrementAndGet();
                    failCount.incrementAndGet();
                }catch (Exception e){
                    log.error("주문 실패", e);
                    failCount.incrementAndGet();
                }finally {
                    latch.countDown();
                }
            });
        }
        latch.await(30, TimeUnit.SECONDS);

        log.info("성공 : {}, 실패 : {}, 낙관적 락 실패 : {}", successCount.get(), failCount.get(), lockFailCount.get());
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(5);

        // DB 확인
        List<Order> successOrders = orderJpaRepository.findAll().stream()
                .filter(order -> order.getGroupPurchaseId().equals(testGroupPurchase.getGroupPurchaseId()))
                .filter(order -> order.getStatus() == OrderStatus.CONFIRMED)
                .toList();

        assertThat(successOrders).hasSize(10);

        List<Order> cancelOrders = orderJpaRepository.findAll().stream()
                .filter(order -> order.getGroupPurchaseId().equals(testGroupPurchase.getGroupPurchaseId()))
                .filter(order -> order.getStatus() == OrderStatus.CANCELLED)
                .toList();

        assertThat(cancelOrders).hasSize(5);

        GroupPurchase updatedGroupPurchase = groupPurchaseRepository.findById(testGroupPurchase.getGroupPurchaseId())
                .orElseThrow();

        assertThat(updatedGroupPurchase.getCurrentQuantity()).isEqualTo(100);
        assertThat(updatedGroupPurchase.getRemainingQuantity()).isEqualTo(0);

        assertThat(updatedGroupPurchase.getStatus()).isEqualTo(GroupPurchaseStatus.SUCCESS);
    }

    private Product createTestProduct() {
        return new Product(
                "테스트 상품",
                20000L,
                ProductCategory.BEAUTY,
                "테스트 상품 설명",
                100,
                null,
                testSellerId
        );
    }

    private GroupPurchase createTestGroupPurchase(){
        return new GroupPurchase(
                10,
                100,
                "테스트 공동구매",
                "테스트 공동구매 설명",
                12000L,
                OffsetDateTime.now().plusHours(1),
                OffsetDateTime.now().plusDays(7),
                testSellerId,
                testProductId
        );
    }
}
