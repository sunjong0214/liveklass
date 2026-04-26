package com.liveklass.controller.lecture;

import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.controller.lecture.dto.LectureDetailResponse;
import com.liveklass.controller.lecture.dto.LectureResponse;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public ResponseEntity<Page<LectureResponse>> findLectures(
            @RequestParam(required = false) final LectureStatus status,
            @PageableDefault(size = 10, sort = "createdAt") final Pageable pageable
    ) {
        return ResponseEntity.ok(lectureService.findLectures(status, pageable));
    }

    @GetMapping("/{lectureId}")
    public ResponseEntity<LectureDetailResponse> getLecture(@PathVariable final Long lectureId) {
        return ResponseEntity.ok(lectureService.getLecture(lectureId));
    }
}
