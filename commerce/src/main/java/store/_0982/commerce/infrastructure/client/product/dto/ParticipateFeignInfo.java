package store._0982.commerce.infrastructure.client.product.dto;

public record ParticipateFeignInfo(
        boolean success,
        String status,
        int remainingQuantity,
        String message
) {

    public static ParticipateFeignInfo success(String status, int remainingQuantity, String message) {
        return new ParticipateFeignInfo(true, status, remainingQuantity, message);
    }

    public static ParticipateFeignInfo failure(String status, int remainingQuantity, String message) {
        return new ParticipateFeignInfo(false, status, remainingQuantity, message);
    }

}
