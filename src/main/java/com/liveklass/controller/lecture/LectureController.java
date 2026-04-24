package com.liveklass.controller.lecture;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.service.lecture.LectureService;

import lombok.RequiredArgsConstructor;

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
	public ResponseEntity<List<Lecture>> findLectures(
		@RequestParam(required = false) final LectureStatus status
	) {
		return ResponseEntity.ok(lectureService.findLectures(status));
	}

	@GetMapping("/{lectureId}")
	public ResponseEntity<Lecture> getLecture(@PathVariable final Long lectureId) {
		return ResponseEntity.ok(lectureService.getLecture(lectureId));
	}
}
