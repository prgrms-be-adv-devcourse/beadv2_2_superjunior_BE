package store._0982.member.presentation;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import store._0982.common.dto.ResponseDto;
import store._0982.member.application.SellerService;
import store._0982.member.application.dto.SellerAccountInfo;
import store._0982.member.presentation.dto.SellerAccountListRequest;

import java.util.List;
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/internal/members")
@Hidden
public class InternalController { //TODO: log 달기

    private final SellerService sellerService;

    @Operation(summary = "판매자 계좌 조회 (내부용)", description = "internal 통신용으로 판매자의 정산 계좌 정보를 조회합니다.") // N+1 문제, -> 리스트 요청, 리스트 응답으로 변경
    @GetMapping("seller-account")
    public ResponseDto<List<SellerAccountInfo>> getSellerAccountInfo(@RequestBody SellerAccountListRequest request) {
        return new ResponseDto<>(HttpStatus.OK, sellerService.getSellerAccountList(request.toCommand()), "판매자 계좌 정보");
    }
}
