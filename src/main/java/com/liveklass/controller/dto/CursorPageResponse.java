package com.liveklass.controller.dto;

import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CursorPageResponse<T> {
    private final List<T> content;
    private final String nextCursorCreatedAt;
    private final Long nextCursorId;
    private final boolean hasNext;

    public CursorPageResponse(List<T> content, LocalDateTime nextCursorCreatedAt, Long nextCursorId, boolean hasNext) {
        this.content = content;
        this.nextCursorCreatedAt = nextCursorCreatedAt != null ? nextCursorCreatedAt.toString() : null;
        this.nextCursorId = nextCursorId;
        this.hasNext = hasNext;
    }
}
