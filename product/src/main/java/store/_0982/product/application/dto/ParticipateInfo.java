package store._0982.product.application.dto;

public record ParticipateInfo(
        boolean success,
        String status,
        int remainingQuantity
) {

    public static ParticipateInfo success(String status, int remainingQuantity) {
        return new ParticipateInfo(true, status, remainingQuantity);
    }

    public static ParticipateInfo failure(String status, int remainingQuantity) {
        return new ParticipateInfo(false, status, remainingQuantity);
    }

}