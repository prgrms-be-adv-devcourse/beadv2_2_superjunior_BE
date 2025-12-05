package store._0982.member.presentation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.member.application.MemberService;
import store._0982.member.application.dto.MemberSignUpInfo;
import store._0982.member.common.dto.ResponseDto;
import store._0982.member.presentation.dto.MemberSignUpRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    final private MemberService memberService;
    @PostMapping
    public ResponseDto<MemberSignUpInfo> signUpMember(@RequestBody MemberSignUpRequest memberSignUpRequest) {
        MemberSignUpInfo memberSignUpInfo = memberService.signUpMember(memberSignUpRequest.toCommand());
        return new ResponseDto<>(HttpStatus.OK, memberSignUpInfo, "회원가입이 완료되었습니다.");
    }

    @GetMapping("/header-check")
    public ResponseDto<?> checkHeader(
            @RequestHeader(value = "X-Member-Id", required = false) String memberId,
            @RequestHeader(value = "X-Member-Email", required = false) String email,
            @RequestHeader(value = "X-Member-Role", required = false) String role
    ) {

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Member-Id", memberId);
        headers.put("X-Member-Email", email);
        headers.put("X-Member-Role", role);

        return new ResponseDto<>(HttpStatus.OK, headers, "정상 헤더 출력");
    }
}
