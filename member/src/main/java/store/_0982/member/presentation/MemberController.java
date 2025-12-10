package store._0982.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.auth.RequireRole;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.member.application.MemberService;
import store._0982.member.application.SellerService;
import store._0982.member.application.dto.*;
import store._0982.member.presentation.dto.*;
import store._0982.common.auth.Role;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Slf4j
//TODO: log 달기
//TODO: 이메일 인증 endpoint
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
    public ResponseDto<Void> deleteMember(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId, @Valid @RequestBody MemberDeleteRequest request) {
        memberService.deleteMember(new MemberDeleteCommand(memberId, request.password()));
        return new ResponseDto<>(HttpStatus.OK, null, "회원탈퇴가 완료되었습니다.");
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 검증한 후 새 비밀번호로 변경합니다.")
    @PutMapping("/password")
    public ResponseDto<Void> changePassword(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId, @Valid @RequestBody PasswordChangeRequest request) {
        PasswordChangeCommand command = new PasswordChangeCommand(memberId, request.password(), request.newPassword());
        memberService.changePassword(command);
        return new ResponseDto<>(HttpStatus.OK, null, "비밀번호가 변경되었습니다.");
    }

    @Operation(summary = "프로필 조회", description = "회원의 기본 프로필 정보를 조회합니다.")
    @GetMapping("/profile")
    public ResponseDto<ProfileInfo> getProfile(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId) {
        return new ResponseDto<>(HttpStatus.OK, memberService.getProfile(memberId), "프로필 정보");
    }

    @Operation(summary = "프로필 수정", description = "회원의 이름 및 전화번호 정보를 수정합니다.")
    @PutMapping("/profile")
    public ResponseDto<ProfileUpdateInfo> updateProfile(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId, @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileUpdateCommand command = new ProfileUpdateCommand(memberId, request.name(), request.phoneNumber());
        return new ResponseDto<>(HttpStatus.OK, memberService.updateProfile(command), "프로필 정보가 변경되었습니다.");
    }

    @Operation(summary = "이름 중복 체크", description = "회원의 이름이 사용가능한지 확인합니다.")
    @GetMapping("/name/{name}")
    public ResponseDto<String> checkNameDuplication(@PathVariable("name") String name) {
        memberService.checkNameDuplication(name);
        return new ResponseDto<>(HttpStatus.OK, name, "사용가능한 이름입니다.");
    }


    @Operation(summary = "헤더 확인", description = "게이트웨이에서 전달된 회원 헤더 정보를 확인합니다.")
    @GetMapping("/header-check")
    public ResponseDto<Map> checkHeader(@RequestHeader(value = "X-Member-Id", required = false) String memberId, @RequestHeader(value = "X-Member-Email", required = false) String email, @RequestHeader(value = "X-Member-Role", required = false) String role) {

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Member-Id", memberId);
        headers.put("X-Member-Email", email);
        headers.put("X-Member-Role", role);

        return new ResponseDto<>(HttpStatus.OK, headers, "정상 헤더 출력");
    }

    //아래는 Seller 관련 endpoint
    @Operation(summary = "판매자 등록", description = "회원이 판매자로 등록합니다.")
    @PostMapping("/seller")
    @RequireRole(Role.CONSUMER)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<SellerRegisterInfo> registerSeller(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId, @RequestHeader(value = "X-Member-Role", required = true) Role role, @Valid @RequestBody SellerRegisterRequest sellerRegisterRequest) {
        SellerRegisterCommand command = sellerRegisterRequest.toCommand(memberId);
        return new ResponseDto<>(HttpStatus.CREATED, sellerService.registerSeller(command), "판매자 등록이 완료되었습니다.");
    }

    @Operation(summary = "판매자 정보 조회", description = "판매자 정보를 조회합니다.")
    @GetMapping("/seller/{sellerId}")
    public ResponseDto<SellerInfo> getSeller(@RequestHeader(value = "X-Member-Id", required = false) UUID memberId, @PathVariable UUID sellerId) {
        SellerCommand command = new SellerCommand(memberId, sellerId);
        return new ResponseDto<>(HttpStatus.OK, sellerService.getSeller(command), "판매자 정보");
    }

    @PutMapping("/seller")
    @RequireRole(Role.SELLER)
    public ResponseDto<SellerRegisterInfo> updateSeller(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId, @RequestHeader(value = "X-Member-Role", required = true) Role role, @Valid @RequestBody SellerRegisterRequest sellerRegisterRequest) {
        SellerRegisterCommand command = sellerRegisterRequest.toCommand(memberId);
        return new ResponseDto<>(HttpStatus.CREATED, sellerService.updateSeller(command), "판매자 정보 수정이 완료되었습니다.");
    }

    //이 아래로는 Address endpoint
    @PostMapping("/address")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseDto<AddressInfo> addAddress(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId, @Valid @RequestBody AddressAddRequest addressAddRequest) {
        AddressAddCommand command = addressAddRequest.toCommand(memberId);
        return new ResponseDto<>(HttpStatus.CREATED,memberService.addAddress(command), "주소 등록이 완료되었습니다.");
    }

    @GetMapping("/addresses")
    public ResponseDto<PageResponse<AddressInfo>> getAddresses(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId,
                                                               org.springframework.data.domain.Pageable pageable) {
        return new ResponseDto<>(HttpStatus.OK, memberService.getAddresses(pageable, memberId), "사용자의 주소 리스트");
    }

    @DeleteMapping("/address/{addressId}")
    public ResponseDto<Void> addAddress(@RequestHeader(value = "X-Member-Id", required = true) UUID memberId, @PathVariable UUID addressId) {
        AddressDeleteCommand command = new AddressDeleteCommand(memberId, addressId);
        memberService.deleteAddress(command);
        return new ResponseDto<>(HttpStatus.OK, null, "주소 삭제가 완료되었습니다.");
    }
}
