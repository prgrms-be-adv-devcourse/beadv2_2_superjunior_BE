package store._0982.commerce.application.s3.dto;

public record PresignedUrlResponse(
        String presignedUrl,
        String imageUrl,
        int expirationSeconds
) {
}
