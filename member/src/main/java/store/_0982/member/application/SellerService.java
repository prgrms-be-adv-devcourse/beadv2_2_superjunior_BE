package store._0982.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.member.application.dto.SellerRegisterCommand;
import store._0982.member.application.dto.SellerRegisterInfo;
import store._0982.member.common.exception.CustomErrorCode;
import store._0982.member.common.exception.CustomException;
import store._0982.member.domain.Member;
import store._0982.member.domain.MemberRepository;
import store._0982.member.domain.Seller;
import store._0982.member.domain.SellerRepository;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final SellerRepository sellerRepository;
    private final MemberRepository memberRepository;

    public SellerRegisterInfo registerSeller(SellerRegisterCommand command) {
        Member member = memberRepository.findById(command.memberId()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_EXIST_MEMBER));
        Seller seller = Seller.create(member, command.bankCode(), command.accountNumber(), command.accountHolder(), command.businessRegistrationNumber());
        return SellerRegisterInfo.from(sellerRepository.save(seller));
    }
}
