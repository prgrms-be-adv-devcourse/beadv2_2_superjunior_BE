package store._0982.commerce.presentation.grouppurchase;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import store._0982.commerce.application.s3.S3ImageUploader;
import store._0982.commerce.application.s3.dto.ImageUploadResponse;
import store._0982.commerce.application.s3.dto.PresignedUrlRequest;
import store._0982.commerce.application.s3.dto.PresignedUrlResponse;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/purchases/images")
public class GroupPurchaseImageController {

    private final S3ImageUploader s3ImageUploader;

    @ControllerLog
    @PostMapping("/presigned-url")
    public ResponseDto<PresignedUrlResponse> getPresignedUrl(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody PresignedUrlRequest request) {

        PresignedUrlResponse response = s3ImageUploader.generatePresignedUrlForGroupPurchase(
                request.fileName(),
                request.contentType(),
                memberId
        );
        return new ResponseDto<>(HttpStatus.OK, response, "Presigned URL 발급 성공");
    }

    @ControllerLog
    @PostMapping
    public ResponseDto<ImageUploadResponse> upload(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestPart("file") MultipartFile file) {

        String imageUrl = s3ImageUploader.upload(file, memberId, S3ImageUploader.ImageType.GROUP_PURCHASE);
        return new ResponseDto<>(HttpStatus.OK, new ImageUploadResponse(imageUrl), "이미지 업로드 성공");
    }

    @ControllerLog
    @DeleteMapping
    public ResponseDto<Void> delete(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestParam String imageUrl) {

        s3ImageUploader.delete(imageUrl);
        return new ResponseDto<>(HttpStatus.OK, null, "이미지 삭제 성공");
    }
}
