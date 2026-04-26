package com.liveklass.controller.enrollment;

import com.liveklass.controller.enrollment.dto.EnrollmentRequest;
import com.liveklass.controller.enrollment.dto.EnrollmentResponse;
import com.liveklass.service.enrollment.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Page<EnrollmentResponse>> getMyEnrollments(
            @RequestHeader("X-Member-Id") final Long memberId,
            @PageableDefault(size = 10, sort = "enrolledAt") final Pageable pageable
    ) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(memberId, pageable));
    }
}
