package com.liveklass.controller.enrollment.dto;

import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.enrollment.EnrollmentStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EnrollmentResponse {
    private final Long id;
    private final Long memberId;
    private final Long lectureId;
    private final EnrollmentStatus status;
    private final LocalDateTime enrolledAt;
    private final LocalDateTime paymentAt;

    public EnrollmentResponse(final Enrollment enrollment) {
        this.id = enrollment.getId();
        this.memberId = enrollment.getMemberId();
        this.lectureId = enrollment.getLectureId();
        this.status = enrollment.getStatus();
        this.enrolledAt = enrollment.getEnrolledAt();
        this.paymentAt = enrollment.getPaymentAt();
    }
}
