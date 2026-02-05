package store._0982.commerce.application.s3.dto;

public record PresignedUrlRequest(
        String fileName,
        String contentType
) {
}
