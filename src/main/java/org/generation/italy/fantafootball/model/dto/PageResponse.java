package org.generation.italy.fantafootball.model.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(List<T> content, int page, int size,
                              long totalElements, int totalPages, boolean hasNext) {
    public static <T> PageResponse<T> fromPage(Page<T> result) {
        return new PageResponse<>(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), result.hasNext());
    }
}
