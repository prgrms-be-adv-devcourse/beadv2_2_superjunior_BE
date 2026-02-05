package store._0982.commerce.presentation.product;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import store._0982.commerce.application.s3.S3ImageUploader;
import store._0982.commerce.application.s3.dto.ImageUploadResponse;
import store._0982.commerce.application.s3.dto.PresignedUrlRequest;
import store._0982.commerce.application.s3.dto.PresignedUrlResponse;
import store._0982.common.HeaderName;
import store._0982.common.dto.ResponseDto;
import store._0982.common.log.ControllerLog;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products/images")
public class ProductImageController {

    private final S3ImageUploader s3ImageUploader;

    @ControllerLog
    @PostMapping("/presigned-url")
    public ResponseDto<PresignedUrlResponse> getPresignedUrl(
            @RequestHeader(HeaderName.ID) UUID memberId,
            @RequestBody PresignedUrlRequest request) {

        PresignedUrlResponse response = s3ImageUploader.generatePresignedUrl(
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

        String imageUrl = s3ImageUploader.upload(file, memberId, S3ImageUploader.ImageType.PRODUCT);
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
