package store._0982.member.presentation.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.member.application.notification.NotificationSettingService;
import store._0982.member.application.notification.dto.NotificationSettingInfo;
import store._0982.member.application.notification.dto.NotificationSettingUpdateCommand;
import store._0982.member.presentation.notification.dto.NotificationSettingUpdateRequest;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification-settings")
@Tag(name = "Notification Setting", description = "알림 설정 API")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @Operation(summary = "알림 수신 여부 변경")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PutMapping
    public ResponseDto<Void> updateNotificationSetting(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody @Valid NotificationSettingUpdateRequest request
    ) {
        notificationSettingService.updateSetting(memberId, NotificationSettingUpdateCommand.from(request));
        return new ResponseDto<>(HttpStatus.OK, null, "알림 설정이 변경되었습니다.");
    }

    @Operation(summary = "알림 수신 여부 조회")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<NotificationSettingInfo> getNotificationSetting(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @ModelAttribute @Valid NotificationSettingUpdateRequest request
    ) {
        NotificationSettingInfo response = notificationSettingService.getSetting(memberId, request);
        return new ResponseDto<>(HttpStatus.OK, response, "알림 설정을 조회했습니다.");
    }
}
