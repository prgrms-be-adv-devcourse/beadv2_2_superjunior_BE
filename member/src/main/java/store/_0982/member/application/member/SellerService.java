package store._0982.member.application.member;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.common.log.ServiceLog;
import store._0982.member.application.member.dto.*;
import store._0982.member.exception.CustomErrorCode;
import store._0982.member.domain.member.Member;
import store._0982.member.domain.member.MemberRepository;
import store._0982.member.domain.member.Seller;
import store._0982.member.domain.member.SellerRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {
    private final SellerRepository sellerRepository;
    private final MemberRepository memberRepository;

    private final CommerceQueryPort commerceQueryPort;

    @ServiceLog
    @Transactional
    public SellerRegisterInfo registerSeller(SellerRegisterCommand command) {
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        Seller seller = Seller.create(member, command.bankCode(), command.accountNumber(), command.accountHolder(), command.businessRegistrationNumber());
        return SellerRegisterInfo.from(sellerRepository.save(seller));
    }

    @ServiceLog
    @Transactional(noRollbackFor = CustomException.class) //사용자에게는 Error 메세지를 보내지만 결과는 커밋
    public void createSellerBalance(UUID sellerId) {
        Seller seller = sellerRepository.findById(sellerId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_SELLER));
        try {
            commerceQueryPort.postSellerBalance(sellerId);
            seller.confirm();
        } catch (FeignException e) {
            sellerRepository.delete(seller);
            seller.getMember().unregisterSeller();
            memberRepository.save(seller.getMember());
            throw new CustomException(CustomErrorCode.INTERNAL_SERVER_ERROR);   // seller_balance는 생성 실패
        }
    }

    @ServiceLog
    @Transactional
    public SellerInfo getSeller(SellerCommand command) {
        Seller foundSeller = sellerRepository.findById(command.searchedSellerId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_SELLER));
        SellerInfo sellerInfo = SellerInfo.from(foundSeller);
        if (!foundSeller.getSellerId().equals(command.userMemberId())) {     //자기 자신을 검색한 게 아닐 경우 민감 정보 null로 마스킹
            sellerInfo = sellerInfo.blind();
        }
        return sellerInfo;
    }

    @ServiceLog
    @Transactional
    public SellerRegisterInfo updateSeller(SellerRegisterCommand command) {
        Seller seller = sellerRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        seller.update(command.bankCode(), command.accountNumber(), command.accountHolder(), command.businessRegistrationNumber());
        return SellerRegisterInfo.from(seller);
    }

    //internal 엔드포인트 로직
    public List<SellerAccountInfo> getSellerAccountList(SellerAccountListCommand command) {
        return sellerRepository.findAllById(command.sellerIds()).stream().map(SellerAccountInfo::from).toList();
    }
}
