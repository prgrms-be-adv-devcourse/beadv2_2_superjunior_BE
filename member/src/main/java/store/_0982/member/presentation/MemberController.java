package store._0982.member.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store._0982.member.application.MemberService;
import store._0982.member.application.dto.MemberSignUpInfo;
import store._0982.member.common.dto.ResponseDto;
import store._0982.member.presentation.dto.MemberSignUpRequest;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {
    final private MemberService memberService;
    @PostMapping
    public ResponseDto<MemberSignUpInfo> signUpMember(@RequestBody MemberSignUpRequest memberSignUpRequest) {
        MemberSignUpInfo memberSignUpInfo = memberService.signUpMember(memberSignUpRequest.toCommand());
        return new ResponseDto<MemberSignUpInfo>(HttpStatus.OK, memberSignUpInfo, "회원가입이 완료되었습니다.");
    }
}
