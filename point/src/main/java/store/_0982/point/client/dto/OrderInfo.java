package store._0982.point.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderInfo(
        int price,
        String status,
        UUID memberId
) {
}
