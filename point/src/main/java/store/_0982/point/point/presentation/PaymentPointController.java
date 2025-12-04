package store._0982.point.point.presentation;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import store._0982.point.common.dto.ResponseDto;
import store._0982.point.point.application.PaymentPointService;
import store._0982.point.point.application.dto.MemberPointInfo;
import store._0982.point.point.application.dto.PaymentPointInfo;
import store._0982.point.point.presentation.dto.PointChargeCreateRequest;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/points")
public class PaymentPointController {

    private final PaymentPointService paymentPointService;

    @PostMapping("/create")
    public ResponseDto<PaymentPointInfo> createPointPayment(@RequestBody PointChargeCreateRequest request){
        return paymentPointService.createPointPayment(request.toCommand());
    }

    //포인트 조회
    @GetMapping("{memberId}")
    public ResponseDto<MemberPointInfo> pointCheck(@PathVariable() UUID memberId){
        return paymentPointService.pointCheck(memberId);
    }

    //포인트 차감
//    @PatchMapping("use")
//    public ResponseDto<MemberPointInfo> pointMinus(@RequestBody PointMinusRequest request){
//
//    }

}
