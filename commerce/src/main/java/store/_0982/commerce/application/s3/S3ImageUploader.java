package store._0982.commerce.application.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import store._0982.commerce.application.s3.dto.PresignedUrlResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class S3ImageUploader {

    public enum ImageType {
        PRODUCT,
        GROUP_PURCHASE
    }

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.region}")
    private String region;

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; //10MB

    public String upload(MultipartFile file, UUID sellerId, ImageType imageType) {
        validateFile(file);
        String key;
        if (imageType == ImageType.PRODUCT) {
            key = generateProductKey(sellerId, file.getOriginalFilename());
        } else {
            key = generateGroupPurchaseKey(sellerId, file.getOriginalFilename());
        }

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
        return getFileUrl(key);
    }

    public PresignedUrlResponse generatePresignedUrl(String fileName, String contentType, UUID sellerId) {
        validateContentType(contentType);
        String key = generateProductKey(sellerId, fileName);

        return createPresignedUrlResponse(key, contentType);
    }

    public PresignedUrlResponse generatePresignedUrlForGroupPurchase(String fileName, String contentType, UUID sellerId) {
        validateContentType(contentType);
        String key = generateGroupPurchaseKey(sellerId, fileName);

        return createPresignedUrlResponse(key, contentType);
    }

    private PresignedUrlResponse createPresignedUrlResponse(String key, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUrlResponse(
                presignedRequest.url().toString(),
                getFileUrl(key),
                600
        );
    }

    public void delete(String imageUrl) {
        String key = extractKeyFromUrl(imageUrl);
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }

    private String generateProductKey(UUID sellerId, String originalFileName) {
        return String.format("products/%s/%s_%s", sellerId, UUID.randomUUID(), originalFileName);
    }

    private String generateGroupPurchaseKey(UUID sellerId, String originalFileName) {
        return String.format("group-purchases/%s/%s_%s", sellerId, UUID.randomUUID(), originalFileName);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        validateContentType(file.getContentType());
    }

    private void validateContentType(String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (jpeg, png, webp만 가능)");
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("파일 확장자를 확인할 수 없습니다.");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private String extractKeyFromUrl(String imageUrl) {
        String prefix = String.format("https://%s.s3.%s.amazonaws.com/", bucket, region);
        if (!imageUrl.startsWith(prefix)) {
            throw new IllegalArgumentException("유효하지 않은 이미지 URL입니다.");
        }
        return imageUrl.substring(prefix.length());
    }

    private String getFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }
}
