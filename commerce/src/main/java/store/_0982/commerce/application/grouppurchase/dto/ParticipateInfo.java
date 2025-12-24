package store._0982.commerce.application.grouppurchase.dto;

public record ParticipateInfo(
        boolean success,
        String status,
        int remainingQuantity,
        String message
) {

    public static ParticipateInfo success(String status, int remainingQuantity, String message) {
        return new ParticipateInfo(true, status, remainingQuantity, message);
    }

    public static ParticipateInfo failure(String status, int remainingQuantity, String message) {
        return new ParticipateInfo(false, status, remainingQuantity, message);
    }

}
