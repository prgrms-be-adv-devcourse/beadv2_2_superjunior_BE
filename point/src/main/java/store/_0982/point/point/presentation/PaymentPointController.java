package store._0982.point.point.presentation;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import store._0982.point.common.dto.ResponseDto;
import store._0982.point.point.application.dto.PaymentPointInfo;
import store._0982.point.point.presentation.dto.PointChargeCreateRequest;

@RequiredArgsConstructor
@RestController("/api/points")
public class PaymentPointController {

    @PostMapping("/create")
    public ResponseDto<PaymentPointInfo> createPointPayment(@RequestBody PointChargeCreateRequest request){
        return null;
    }
}
