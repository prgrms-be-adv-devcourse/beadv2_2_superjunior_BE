package store._0982.product.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import store._0982.product.application.dto.GroupPurchaseInfo;
import store._0982.product.common.dto.PageResponseDto;
import store._0982.product.common.exception.CustomErrorCode;
import store._0982.product.common.exception.CustomException;
import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final GroupPurchaseRepository groupPurchaseRepository;

    public GroupPurchaseInfo getGroupPurchaseById(UUID groupPurchaseId) {
        GroupPurchase findGroupPurchase = groupPurchaseRepository.findById(groupPurchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));
        // TODO : 참가자 수 집계 기능
        int participantCount = 0;
        return GroupPurchaseInfo.from(findGroupPurchase, participantCount);
    }

    public PageResponseDto<GroupPurchaseInfo> getGroupPurchase(Pageable pageable) {
        Page<GroupPurchase> groupPurchasePage = groupPurchaseRepository.findAll(pageable);

        Page<GroupPurchaseInfo> groupPurchaseInfoPage = groupPurchasePage.map(
                groupPurchase -> {
                    // TODO : 참가자 수 집계 기능
                    int participantCount = 0;
                    return GroupPurchaseInfo.from(groupPurchase, participantCount);
                });

        return PageResponseDto.from(groupPurchaseInfoPage);
    }

    public PageResponseDto<GroupPurchaseInfo> getGroupPurchasesBySeller(UUID sellerId, Pageable pageable) {
        Page<GroupPurchase> groupPurchasePage = groupPurchaseRepository.findAllBySellerId(sellerId, pageable);

        Page<GroupPurchaseInfo> groupPurchaseInfoPage = groupPurchasePage.map(
                groupPurchase -> {
                    // TODO : 참가자 수 집계 기능
                    int participantCount = 0;
                    return GroupPurchaseInfo.from(groupPurchase, participantCount);
                });

        return PageResponseDto.from(groupPurchaseInfoPage);
    }
}
