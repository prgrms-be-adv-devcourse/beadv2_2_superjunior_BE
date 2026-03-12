package store._0982.recommendation.application.dto;

public enum IntentType {
    GREETING,
    PRODUCT,
    GENERIC_RECOMMENDATION,
    SERVICE,
    NON_PRODUCT,
    OFF_TOPIC,
    ABUSIVE;

    public static IntentType from(String value) {
        if (value == null || value.isBlank()) return OFF_TOPIC;
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return OFF_TOPIC;
        }
    }
}
