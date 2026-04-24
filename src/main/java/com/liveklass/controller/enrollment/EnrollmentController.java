package com.liveklass.controller.enrollment;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.liveklass.controller.enrollment.dto.EnrollmentRequest;
import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.service.enrollment.EnrollmentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

	private final EnrollmentService enrollmentService;

	@PostMapping
	public ResponseEntity<Long> enroll(
		@RequestHeader("X-Member-Id") final Long memberId,
		@RequestBody final EnrollmentRequest request
	) {
		return ResponseEntity.ok(enrollmentService.enroll(memberId, request.getLectureId()));
	}

	@PostMapping("/{enrollmentId}/confirm")
	public ResponseEntity<Void> confirmPayment(@PathVariable final Long enrollmentId) {
		enrollmentService.confirmPayment(enrollmentId);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{enrollmentId}/cancel")
	public ResponseEntity<Void> cancelEnrollment(@PathVariable final Long enrollmentId) {
		enrollmentService.cancelEnrollment(enrollmentId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/me")
	public ResponseEntity<List<Enrollment>> getMyEnrollments(
		@RequestHeader("X-Member-Id") final Long memberId
	) {
		return ResponseEntity.ok(enrollmentService.getMyEnrollments(memberId));
	}
}
