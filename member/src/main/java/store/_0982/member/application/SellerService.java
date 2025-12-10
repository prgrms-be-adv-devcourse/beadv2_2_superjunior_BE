package store._0982.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.common.exception.CustomException;
import store._0982.member.application.dto.SellerCommand;
import store._0982.member.application.dto.SellerInfo;
import store._0982.member.application.dto.SellerRegisterCommand;
import store._0982.member.application.dto.SellerRegisterInfo;
import store._0982.member.common.exception.CustomErrorCode;
import store._0982.member.domain.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerService {
    private final SellerRepository sellerRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public SellerRegisterInfo registerSeller(SellerRegisterCommand command) {
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        Seller seller = Seller.create(member, command.bankCode(), command.accountNumber(), command.accountHolder(), command.businessRegistrationNumber());
        return SellerRegisterInfo.from(sellerRepository.save(seller));
    }

    public SellerInfo getSeller(SellerCommand command) {
        Seller foundSeller = sellerRepository.findById(command.searchedSellerId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_SELLER));
        SellerInfo sellerInfo = SellerInfo.from(foundSeller);
        if (!foundSeller.getSellerId().equals(command.userMemberId())) {     //자기 자신을 검색한 게 아닐 경우 민감 정보 null로 마스킹
            sellerInfo = sellerInfo.blind();
        }
        return sellerInfo;
    }

    @Transactional
    public SellerRegisterInfo updateSeller(SellerRegisterCommand command) {
        Seller seller = sellerRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        seller.update(command.bankCode(), command.accountNumber(), command.accountHolder(), command.businessRegistrationNumber());
        return SellerRegisterInfo.from(seller);
    }

}
