package com.liveklass.controller.lecture;

import com.liveklass.controller.dto.CursorPageResponse;
import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.controller.lecture.dto.LectureDetailResponse;
import com.liveklass.controller.lecture.dto.LectureResponse;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/lectures")
@RequiredArgsConstructor
public class LectureController {

    private final LectureService lectureService;

    @PostMapping
    public ResponseEntity<Long> createLecture(
            @RequestHeader("X-Creator-Id") final Long creatorId,
            @RequestBody final LectureCreateRequest request
    ) {
        return ResponseEntity.ok(lectureService.createLecture(creatorId, request));
    }

    @PatchMapping("/{lectureId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable final Long lectureId,
            @RequestParam final LectureStatus status
    ) {
        lectureService.updateStatus(lectureId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<LectureResponse>> findLectures(
            @RequestParam(required = false) final LectureStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final LocalDateTime lastCreatedAt,
            @RequestParam(required = false) final Long lastId,
            @RequestParam(defaultValue = "10") final int size
    ) {
        return ResponseEntity.ok(lectureService.findLectures(status, lastCreatedAt, lastId, size));
    }

    @GetMapping("/{lectureId}")
    public ResponseEntity<LectureDetailResponse> getLecture(@PathVariable final Long lectureId) {
        return ResponseEntity.ok(lectureService.getLecture(lectureId));
    }
}
