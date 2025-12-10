package store._0982.notification.presentation;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import store._0982.common.HeaderName;
import store._0982.common.dto.PageResponse;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;
import store._0982.notification.application.NotificationService;
import store._0982.notification.application.dto.NotificationInfo;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "알림 읽음 처리", description = "알림 하나를 읽음 처리합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PatchMapping("/{id}/read")
    public ResponseDto<Void> read(
            @PathVariable UUID id,
            @RequestHeader(HeaderName.ID) UUID memberId
    ) {
        notificationService.read(memberId, id);
        return new ResponseDto<>(HttpStatus.OK, null, "정상적으로 읽었습니다.");
    }

    @Operation(summary = "알림 전체 읽음 처리", description = "알림 전체를 읽음 처리합니다.")
    @ResponseStatus(HttpStatus.OK)
    @ControllerLog
    @PatchMapping("/read")
    public ResponseDto<Void> readAll(@RequestHeader(HeaderName.ID) UUID memberId) {
        notificationService.readAll(memberId);
        return new ResponseDto<>(HttpStatus.OK, null, "모든 알림을 정상적으로 읽었습니다.");
    }

    @Operation(summary = "알림 목록 조회", description = "유저의 알림 목록을 조회합니다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public ResponseDto<PageResponse<NotificationInfo>> getNotifications(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<NotificationInfo> response = notificationService.getNotifications(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, response, "알림 목록 조회에 성공했습니다.");
    }

    @Operation(summary = "읽지 않은 알림 목록 조회", description = "유저의 읽지 않은 알림 목록을 조회합니다.")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/unread")
    public ResponseDto<PageResponse<NotificationInfo>> getUnreadNotifications(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<NotificationInfo> response = notificationService.getUnreadNotifications(memberId, pageable);
        return new ResponseDto<>(HttpStatus.OK, response, "읽지 않은 알림 목록 조회에 성공했습니다.");
    }
}
