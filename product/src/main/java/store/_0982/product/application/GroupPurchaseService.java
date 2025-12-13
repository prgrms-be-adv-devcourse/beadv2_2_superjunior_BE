package store._0982.product.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.kafka.KafkaTopics;
import store._0982.common.kafka.dto.GroupPurchaseEvent;
import store._0982.common.log.ServiceLog;
import store._0982.product.application.dto.GroupPurchaseDetailInfo;
import store._0982.product.application.dto.GroupPurchaseThumbnailInfo;
import store._0982.product.application.dto.GroupPurchaseRegisterCommand;
import store._0982.product.application.dto.GroupPurchaseInfo;
import store._0982.product.application.dto.GroupPurchaseUpdateCommand;
import store._0982.product.client.MemberClient;
import store._0982.product.application.dto.*;
import store._0982.product.common.dto.PageResponseDto;
import store._0982.product.common.exception.CustomErrorCode;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Transactional
@Service
@RequiredArgsConstructor
public class GroupPurchaseService {
    private final GroupPurchaseRepository groupPurchaseRepository;
    private final ProductRepository productRepository;


    private final KafkaTemplate<String, GroupPurchaseEvent> upsertKafkaTemplate;
    private final MemberClient memberClient;

    /**
     * 공동 구매 생성
     * @param memberId 로그인 유저
     * @param memberRole 로그인 유저 권한
     * @param command 생성할 command 데이터
     * @return PurchaseRegisterInfo
     */
    public GroupPurchaseInfo createGroupPurchase(UUID memberId, String memberRole, GroupPurchaseRegisterCommand command) {
        // 권한 확인
        if(!memberRole.equals("SELLER") && !memberRole.equals("ADMIN")){
            throw new CustomException(CustomErrorCode.NON_SELLER_ACCESS_DENIED);
        }

        // 상품 있는지 확인
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        // 상품이 신청한 사람의 상품이 맞는지 확인
        if(!product.getSellerId().equals(memberId)){
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_PRODUCT_OWNER);
        }

        // 입력값 검증
        if(command.minQuantity() > command.maxQuantity()){
            throw new CustomException(CustomErrorCode.INVALID_QUANTITY_RANGE);
        }

        if(command.startDate().isAfter(command.endDate())){
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

        //kafka
        String productName = product.getName();
        String sellerName = memberClient.getMember(product.getSellerId()).data().name();
        GroupPurchaseEvent event = groupPurchase.toEvent(productName, sellerName, GroupPurchaseEvent.SearchKafkaStatus.CREATE_GROUP_PURCHASE);
        upsertKafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_ADDED,event.getId().toString(), event);

        return GroupPurchaseInfo.from(saved);
    }

    @Transactional(readOnly = true)
    public GroupPurchaseDetailInfo getGroupPurchaseById(UUID purchaseId) {
        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        Product findProduct = productRepository.findById(findGroupPurchase.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));

        return GroupPurchaseDetailInfo.from(findGroupPurchase, findProduct.getOriginalUrl(), findProduct.getPrice());
    }

    @Transactional(readOnly = true)
    public PageResponseDto<GroupPurchaseThumbnailInfo> getGroupPurchase(Pageable pageable) {
        Page<GroupPurchase> groupPurchasePage = groupPurchaseRepository.findAll(pageable);

        Page<GroupPurchaseThumbnailInfo> groupPurchaseInfoPage = groupPurchasePage.map(
                GroupPurchaseThumbnailInfo::from);

        return PageResponseDto.from(groupPurchaseInfoPage);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<GroupPurchaseThumbnailInfo> getGroupPurchasesBySeller(UUID sellerId, Pageable pageable) {
        Page<GroupPurchase> groupPurchasePage = groupPurchaseRepository.findAllBySellerId(sellerId, pageable);

        Page<GroupPurchaseThumbnailInfo> groupPurchaseInfoPage = groupPurchasePage.map(
                GroupPurchaseThumbnailInfo::from);

        return PageResponseDto.from(groupPurchaseInfoPage);
    }

    @ServiceLog
    public void deleteGroupPurchase(UUID purchaseId, UUID memberId) {
        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (!findGroupPurchase.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_GROUP_PURCHASE_OWNER);
        }
        groupPurchaseRepository.delete(findGroupPurchase);

        //search kafka
        GroupPurchaseEvent event = findGroupPurchase.toEvent("", "", GroupPurchaseEvent.SearchKafkaStatus.DELETE_GROUP_PURCHASE);
        upsertKafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED,event.getId().toString(), event);
    }

    /**
     * 공동 구매 업데이트
     * @param memberId 로그인 유저
     * @param memberRole 로그인 유저 권한
     * @param purchaseId 공동구매 Id
     * @return PurchaseDetailInfo
     */
    public GroupPurchaseInfo updateGroupPurchase(UUID memberId, String memberRole, UUID purchaseId, GroupPurchaseUpdateCommand command) {
        // 권한 확인
        if(!memberRole.equals("SELLER") && !memberRole.equals("ADMIN")){
            throw new CustomException(CustomErrorCode.NON_SELLER_ACCESS_DENIED);
        }

        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (!findGroupPurchase.getSellerId().equals(memberId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_NOT_GROUP_PURCHASE_OWNER);
        }

        // 공동 구매가 OPEN 상태일 때 일부 필드 변경 불가
        if(findGroupPurchase.getStatus().equals(GroupPurchaseStatus.OPEN) 
        && (!Objects.equals(findGroupPurchase.getMaxQuantity(), command.maxQuantity())||
            !Objects.equals(findGroupPurchase.getDiscountedPrice(), command.discountedPrice())||
            !Objects.equals(findGroupPurchase.getProductId(), command.productId()))){
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

        GroupPurchase saved =  groupPurchaseRepository.saveAndFlush(findGroupPurchase);

        //search kafka
        Product product = productRepository.findById(saved.getProductId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.PRODUCT_NOT_FOUND));
        String sellerName = memberClient.getMember(product.getSellerId()).data().name();
        GroupPurchaseEvent event = saved.toEvent(product.getName(), sellerName, GroupPurchaseEvent.SearchKafkaStatus.UPDATE_GROUP_PURCHASE);
        upsertKafkaTemplate.send(KafkaTopics.GROUP_PURCHASE_STATUS_CHANGED, event.getId().toString(), event);

        return GroupPurchaseInfo.from(saved);
    }

    @Transactional(readOnly = true)
    public List<GroupPurchaseInternalInfo> getUnsettledGroupPurchases() {
        List<GroupPurchase> unsettledGroupPurchases = groupPurchaseRepository
                .findByStatusAndSettledAtIsNull(GroupPurchaseStatus.SUCCESS);

        return unsettledGroupPurchases.stream()
                .map(GroupPurchaseInternalInfo::from)
                .toList();
    }

    public void markAsSettled(UUID groupPurchaseId) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if (groupPurchase.isSettled()) {
            return;
        }

        groupPurchase.markAsSettled();
        groupPurchaseRepository.save(groupPurchase);
    }

}
