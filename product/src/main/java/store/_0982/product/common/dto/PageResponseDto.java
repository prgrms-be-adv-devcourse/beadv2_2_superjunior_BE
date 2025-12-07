package store._0982.product.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(
        List<T> content,
        int totalPages,
        long totalElements,
        boolean first,
        boolean last,
        int size,
        int numberOfElements
) {
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.isLast(),
                page.getSize(),
                page.getNumberOfElements()
        );
    }
}
