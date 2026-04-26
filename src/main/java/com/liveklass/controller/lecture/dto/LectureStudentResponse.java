package com.liveklass.controller.lecture.dto;

import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.enrollment.EnrollmentStatus;
import com.liveklass.domain.member.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LectureStudentResponse {
    private final Long memberId;
    private final String name;
    private final String email;
    private final EnrollmentStatus status;
    private final LocalDateTime enrolledAt;

    public LectureStudentResponse(final Member member, final Enrollment enrollment) {
        this.memberId = member.getId();
        this.name = member.getName();
        this.email = member.getEmail();
        this.status = enrollment.getStatus();
        this.enrolledAt = enrollment.getEnrolledAt();
    }
}
