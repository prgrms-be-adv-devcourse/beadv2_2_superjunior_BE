package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.dto.*;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseCreatedEvent;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseDeletedEvent;
import store._0982.commerce.application.grouppurchase.event.GroupPurchaseUpdatedEvent;
import store._0982.common.dto.PageResponse;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.commerce.infrastructure.client.member.MemberClient;
import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.Product;
import store._0982.commerce.domain.product.ProductRepository;
import store._0982.commerce.exception.CustomErrorCode;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class GroupPurchaseService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductRepository productRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final MemberClient memberClient;

    /**
     * 공동 구매 생성
     *
     * @param memberId 로그인 유저
     * @param command  생성할 command 데이터
     * @return PurchaseRegisterInfo
     */
    @Transactional
    public GroupPurchaseInfo createGroupPurchase(UUID memberId, GroupPurchaseRegisterCommand command) {

        // 상품 있는지 확인
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        // 상품이 신청한 사람의 상품이 맞는지 확인
        if (!product.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_PRODUCT_OWNER);
        }

        // 입력값 검증
        if (command.minQuantity() > command.maxQuantity()) {
            throw new CustomException(CustomErrorCode.INVALID_QUANTITY_RANGE);
        }

        if (command.startDate().isAfter(command.endDate())) {
            throw new CustomException(CustomErrorCode.INVALID_DATE_RANGE);
        }
        GroupPurchase groupPurchase = new GroupPurchase(
                command.minQuantity(),
                command.maxQuantity(),
                command.title(),
                command.description(),
                command.discountedPrice(),
                command.startDate(),
                command.endDate(),
                memberId,
                command.productId()
        );

        GroupPurchase saved = groupPurchaseRepository.saveAndFlush(groupPurchase);

        // 검색 서비스용 Kafka 이벤트 발행
        String sellerName = memberClient.getMember(product.getSellerId()).data().name();
        eventPublisher.publishEvent(
                new GroupPurchaseCreatedEvent(saved, sellerName, product)
        );

        return GroupPurchaseInfo.from(saved);
    }

    public GroupPurchaseDetailInfo getGroupPurchaseById(UUID purchaseId) {
        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        Product findProduct = productRepository.findById(findGroupPurchase.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        return GroupPurchaseDetailInfo.from(findGroupPurchase, findProduct.getOriginalUrl(), findProduct.getPrice(), findProduct.getCategory());
    }

    public PageResponse<GroupPurchaseThumbnailInfo> getGroupPurchase(Pageable pageable) {
        Page<GroupPurchase> groupPurchasePage = groupPurchaseRepository.findAll(pageable);

        Page<GroupPurchaseThumbnailInfo> groupPurchaseInfoPage = groupPurchasePage.map(groupPurchase -> {
            Product product = productRepository.findById(groupPurchase.getProductId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));
            return GroupPurchaseThumbnailInfo.from(groupPurchase, product.getCategory());
        });

        return PageResponse.from(groupPurchaseInfoPage);
    }

    public List<GroupPurchase> getGroupPurchaseByIds(List<UUID> purchaseIds) {
        return groupPurchaseRepository.findAllByGroupPurchaseIdIn(purchaseIds);
    }

    public PageResponse<GroupPurchaseThumbnailInfo> getGroupPurchasesBySeller(UUID sellerId, Pageable pageable) {
        Page<GroupPurchase> groupPurchasePage = groupPurchaseRepository.findAllBySellerId(sellerId, pageable);

        Page<GroupPurchaseThumbnailInfo> groupPurchaseInfoPage = groupPurchasePage.map(groupPurchase -> {
            Product product = productRepository.findById(groupPurchase.getProductId())
                    .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));
            return GroupPurchaseThumbnailInfo.from(groupPurchase, product.getCategory());
        });

        return PageResponse.from(groupPurchaseInfoPage);
    }

    /**
     * 공동 구매 업데이트
     *
     * @param memberId   로그인 유저
     * @param purchaseId 공동구매 Id
     * @return PurchaseDetailInfo
     */
    @Transactional
    public GroupPurchaseInfo updateGroupPurchase(UUID memberId, UUID purchaseId, GroupPurchaseUpdateCommand command) {

        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (!findGroupPurchase.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_GROUP_PURCHASE_OWNER);
        }

        // 공동 구매가 OPEN 상태일 때 일부 필드 변경 불가
        if (findGroupPurchase.getStatus().equals(GroupPurchaseStatus.OPEN)
                && (!Objects.equals(findGroupPurchase.getMaxQuantity(), command.maxQuantity()) ||
                !Objects.equals(findGroupPurchase.getDiscountedPrice(), command.discountedPrice()) ||
                !Objects.equals(findGroupPurchase.getProductId(), command.productId()))) {
            throw new CustomException(CustomErrorCode.INVALID_OPEN_PURCHASE_UPDATE);
        }

        findGroupPurchase.updateGroupPurchase(
                command.minQuantity(),
                command.maxQuantity(),
                command.title(),
                command.description(),
                command.discountedPrice(),
                command.startDate(),
                command.endDate(),
                command.productId()
        );

        GroupPurchase saved = groupPurchaseRepository.saveAndFlush(findGroupPurchase);

        // 검색 서비스용 Kafka 이벤트 발행
        Product product = productRepository.findById(saved.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));
        String sellerName = memberClient.getMember(product.getSellerId()).data().name();
        eventPublisher.publishEvent(
                new GroupPurchaseUpdatedEvent(saved, sellerName, product)
        );

        return GroupPurchaseInfo.from(saved);
    }

    @ServiceLog
    @Transactional
    public void deleteGroupPurchase(UUID purchaseId, UUID memberId) {
        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (findGroupPurchase.getStatus() != GroupPurchaseStatus.SCHEDULED) {
            throw new CustomException(CustomErrorCode.INVALID_OPEN_PURCHASE_UPDATE);
        }
        if (!findGroupPurchase.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_GROUP_PURCHASE_OWNER);
        }
        groupPurchaseRepository.delete(findGroupPurchase);

        // 검색 서비스용 Kafka 이벤트 발행
        eventPublisher.publishEvent(
                new GroupPurchaseDeletedEvent(findGroupPurchase)
        );
    }

    public List<GroupPurchaseInternalInfo> getUnsettledGroupPurchases() {
        OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minusWeeks(2);

        List<GroupPurchase> unsettledGroupPurchases = groupPurchaseRepository
                .findByStatusAndSettledAtIsNull(GroupPurchaseStatus.SUCCESS);

        return unsettledGroupPurchases.stream()
                .filter(gp -> gp.getEndDate().isBefore(twoWeeksAgo))
                .map(GroupPurchaseInternalInfo::from)
                .toList();
    }

    @Transactional
    public void markAsSettled(UUID groupPurchaseId) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (groupPurchase.isSettled()) {
            return;
        }

        groupPurchase.markAsSettled();
        groupPurchaseRepository.save(groupPurchase);
    }

    public GroupPurchase getAvailableForOrder(UUID groupPurchaseId){
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        // 상태확인
        if (groupPurchase.getStatus() != GroupPurchaseStatus.OPEN) {
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_NOT_OPEN);
        }

        // 종료시간 확인
        if (groupPurchase.getEndDate().isBefore(OffsetDateTime.now())) {
            throw new CustomException(CustomErrorCode.GROUP_PURCHASE_IS_END);
        }

        return groupPurchase;
    }
}
