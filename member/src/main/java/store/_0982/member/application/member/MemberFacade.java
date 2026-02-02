package store._0982.member.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.member.application.member.dto.MemberSignUpCommand;
import store._0982.member.application.member.dto.MemberSignUpInfo;
import store._0982.member.application.member.dto.SellerRegisterCommand;
import store._0982.member.application.member.dto.SellerRegisterInfo;
import store._0982.member.application.notification.NotificationSettingService;

@Service
@RequiredArgsConstructor
public class MemberFacade {
    private final NotificationSettingService notificationSettingService;
    private final MemberService memberService;

    private final SellerService sellerService;

    public MemberSignUpInfo createMember(MemberSignUpCommand command) {
        memberService.checkEmailDuplication(command.email());
        memberService.checkEmailVerification(command.email());
//        memberService.checkNameDuplication(command.name());
        MemberSignUpInfo memberSignUpInfo = memberService.createMember(command);
        try{
            notificationSettingService.initializeSettings(memberSignUpInfo.memberId());
            memberService.createPointBalance(memberSignUpInfo.memberId());
        } catch (Exception e) {
            memberService.cancelMemberCreation(memberSignUpInfo.memberId());
        }
        return memberSignUpInfo;
    }

    public SellerRegisterInfo registerSeller(SellerRegisterCommand command) {
        SellerRegisterInfo sellerRegisterInfo = sellerService.registerSeller(command);
        sellerService.createSellerBalance(sellerRegisterInfo.sellerId());
        return sellerRegisterInfo;
    }

    public MemberSignUpInfo createGoogleMember(MemberSignUpCommand command){
        MemberSignUpInfo memberSignUpInfo = memberService.createMember(command);
        try{
            notificationSettingService.initializeSettings(memberSignUpInfo.memberId());
            memberService.createPointBalance(memberSignUpInfo.memberId());
        } catch (Exception e) {
            memberService.cancelMemberCreation(memberSignUpInfo.memberId());
        }
        return memberSignUpInfo;
    }

}
