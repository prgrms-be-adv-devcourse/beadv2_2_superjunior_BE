package store._0982.commerce.application.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseLike;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseLikeRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.commerce.exception.CustomErrorCode;
import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.exception.CustomException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupPurchaseLikeService {

    private final GroupPurchaseRepository groupPurchaseRepository;
    private final GroupPurchaseLikeRepository groupPurchaseLikeRepository;

    @Transactional
    public void likeGroupPurchase(UUID memberId, UUID purchaseId) {
        GroupPurchase groupPurchase = groupPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.GROUPPURCHASE_NOT_FOUND));

        if(groupPurchaseLikeRepository.existsByMemberIdAndGroupPurchaseId(memberId, groupPurchase.getGroupPurchaseId())){
            throw new CustomException(CustomErrorCode.ALREADY_LIKED);
        }

        GroupPurchaseLike like = new GroupPurchaseLike(memberId, groupPurchase.getGroupPurchaseId());

        groupPurchaseLikeRepository.save(like);

        groupPurchaseRepository.increaseLikeCount(groupPurchase.getGroupPurchaseId());
    }

    @Transactional
    public void unlikeGroupPurchase(UUID memberId, UUID purchaseId) {
        GroupPurchaseLike like = groupPurchaseLikeRepository.findByMemberIdAndGroupPurchaseId(memberId,purchaseId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.LIKE_NOT_FOUND));

        groupPurchaseLikeRepository.delete(like);
        groupPurchaseRepository.decreaseLikeCount(purchaseId);
    }
}
