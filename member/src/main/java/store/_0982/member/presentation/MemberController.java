package store._0982.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.auth.RequireRole;
import store._0982.common.auth.Role;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.member.application.MemberService;
import store._0982.member.application.SellerService;
import store._0982.member.application.dto.*;
import store._0982.member.presentation.dto.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
//TODO: log 달기

public class MemberController {
    private final MemberService memberService;
    private final SellerService sellerService;

    @Operation(summary = "회원가입", description = "신규 회원을 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<MemberSignUpInfo> createMember(@Valid @RequestBody MemberSignUpRequest memberSignUpRequest) {
        MemberSignUpInfo memberSignUpInfo = memberService.createMember(memberSignUpRequest.toCommand());
        return new ResponseDto<>(HttpStatus.CREATED, memberSignUpInfo, "회원가입이 완료되었습니다.");
    }

    @Operation(summary = "회원 탈퇴", description = "비밀번호를 확인하고 회원을 탈퇴 처리합니다.")
    @DeleteMapping
    public ResponseDto<Void> deleteMember(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody MemberDeleteRequest request) {
        memberService.deleteMember(new MemberDeleteCommand(memberId, request.password()));
        return new ResponseDto<>(HttpStatus.OK, null, "회원탈퇴가 완료되었습니다.");
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 검증한 후 새 비밀번호로 변경합니다.")
    @PutMapping("/password")
    public ResponseDto<Void> changePassword(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody PasswordChangeRequest request) {
        PasswordChangeCommand command = new PasswordChangeCommand(memberId, request.password(), request.newPassword());
        memberService.changePassword(command);
        return new ResponseDto<>(HttpStatus.OK, null, "비밀번호가 변경되었습니다.");
    }

    @Operation(summary = "프로필 조회", description = "회원의 기본 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseDto<ProfileInfo> getProfile(@RequestHeader(value = HeaderName.ID) UUID memberId) {
        return new ResponseDto<>(HttpStatus.OK, memberService.getProfile(memberId), "프로필 정보");
    }

    @Operation(summary = "프로필 수정", description = "회원의 이름 및 전화번호 정보를 수정합니다.")
    @PutMapping("/profile")
    public ResponseDto<ProfileUpdateInfo> updateProfile(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileUpdateCommand command = new ProfileUpdateCommand(memberId, request.name(), request.phoneNumber());
        return new ResponseDto<>(HttpStatus.OK, memberService.updateProfile(command), "프로필 정보가 변경되었습니다.");
    }

    @Operation(summary = "이름 중복 체크", description = "회원의 이름이 사용가능한지 확인합니다.")
    @GetMapping("/name/{name}")
    public ResponseDto<String> checkNameDuplication(@PathVariable("name") String name) {
        memberService.checkNameDuplication(name);
        return new ResponseDto<>(HttpStatus.OK, name, "사용가능한 이름입니다.");
    }

    @Operation(summary = "이메일 인증 메일 전송", description = "입력한 이메일 주소로 인증 메일을 전송합니다.")
    @GetMapping("email/{email}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<String> sendVerificationEmail(@PathVariable("email") String email) {
        memberService.sendVerificationEmail(email);
        return new ResponseDto<>(HttpStatus.CREATED, email, "이메일 인증 메일을 보냈습니다. 이메일을 확인해주세요.");
    }

    @Operation(summary = "이메일 인증", description = "인증 메일에 포함된 토큰으로 이메일 인증을 완료합니다.")
    @GetMapping("email/verification/{token}")
    public ResponseDto<String> verifyEmail(@PathVariable("token") String token) {
        memberService.verifyEmail(token);
        return new ResponseDto<>(HttpStatus.OK, null, "이메일 인증이 완료되었습니다.");
    }

    //아래는 Seller 관련 endpoint
    @Operation(summary = "판매자 등록", description = "회원이 판매자로 등록합니다.")
    @PostMapping("/seller")
    @RequireRole(Role.CONSUMER)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<SellerRegisterInfo> registerSeller(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody SellerRegisterRequest sellerRegisterRequest) {
        SellerRegisterCommand command = sellerRegisterRequest.toCommand(memberId);
        return new ResponseDto<>(HttpStatus.CREATED, sellerService.registerSeller(command), "판매자 등록이 완료되었습니다.");
    }

    @Operation(summary = "판매자 정보 조회", description = "판매자 정보를 조회합니다.")
    @GetMapping("/seller/{sellerId}")
    public ResponseDto<SellerInfo> getSeller(@RequestHeader(value = HeaderName.ID, required = false) UUID memberId, @PathVariable UUID sellerId) {
        SellerCommand command = new SellerCommand(memberId, sellerId);
        return new ResponseDto<>(HttpStatus.OK, sellerService.getSeller(command), "판매자 정보");
    }

    @Operation(summary = "판매자 정보 수정", description = "판매자 정보를 수정합니다.")
    @PutMapping("/seller")
    @RequireRole(Role.SELLER)
    public ResponseDto<SellerRegisterInfo> updateSeller(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody SellerRegisterRequest sellerRegisterRequest) {
        SellerRegisterCommand command = sellerRegisterRequest.toCommand(memberId);
        return new ResponseDto<>(HttpStatus.OK, sellerService.updateSeller(command), "판매자 정보 수정이 완료되었습니다.");
    }

    //이 아래로는 Address endpoint
    @Operation(summary = "주소 등록", description = "회원의 주소를 추가합니다.")
    @PostMapping("/address")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<AddressInfo> addAddress(@RequestHeader(value = HeaderName.ID) UUID memberId, @Valid @RequestBody AddressAddRequest addressAddRequest) {
        AddressAddCommand command = addressAddRequest.toCommand(memberId);
        return new ResponseDto<>(HttpStatus.CREATED, memberService.addAddress(command), "주소 등록이 완료되었습니다.");
    }

    @Operation(summary = "주소 목록 조회", description = "회원의 주소 목록을 페이지네이션하여 조회합니다.")
    @GetMapping("/addresses")
    public ResponseDto<PageResponse<AddressInfo>> getAddresses(@RequestHeader(value = HeaderName.ID) UUID memberId, org.springframework.data.domain.Pageable pageable) {
        return new ResponseDto<>(HttpStatus.OK, memberService.getAddresses(pageable, memberId), "사용자의 주소 리스트");
    }

    @Operation(summary = "주소 삭제", description = "회원의 주소를 삭제합니다.")
    @DeleteMapping("/address/{addressId}")
    public ResponseDto<Void> addAddress(@RequestHeader(value = HeaderName.ID) UUID memberId, @PathVariable UUID addressId) {
        AddressDeleteCommand command = new AddressDeleteCommand(memberId, addressId);
        memberService.deleteAddress(command);
        return new ResponseDto<>(HttpStatus.OK, null, "주소 삭제가 완료되었습니다.");
    }

    @Operation(summary = "헤더 확인", description = "게이트웨이에서 전달된 회원 헤더 정보를 확인합니다.")
    @GetMapping("/header-check")
    public ResponseDto<Map<String, String>> checkHeader(@RequestHeader(value = HeaderName.ID, required = false) String memberId, @RequestHeader(value = HeaderName.EMAIL, required = false) String email, @RequestHeader(value = HeaderName.ROLE, required = false) String role) {

        Map<String, String> headers = new HashMap<>();
        headers.put(HeaderName.ID, memberId);
        headers.put(HeaderName.EMAIL, email);
        headers.put(HeaderName.ROLE, role);

        return new ResponseDto<>(HttpStatus.OK, headers, "정상 헤더 출력");
    }

    @Operation(summary = "판매자 계좌 조회 (내부용)", description = "internal 통신용으로 판매자의 정산 계좌 정보를 조회합니다.") //TODO: 판매자 수만큼 호출은 N+1 문제, -> 리스트 요청, 리스트 응답으로 변경 필요
    @GetMapping("/internal/seller-account/{sellerId}")
    public ResponseDto<SellerAccountInfo> getSellerAccountInfo(@PathVariable UUID sellerId){
        SellerAccountInfo sellerAccountInfo = sellerService.getSellerAccountInfo(sellerId);
        return new ResponseDto<>(HttpStatus.OK, sellerAccountInfo, "판매자 계좌 정보");
    }

}
