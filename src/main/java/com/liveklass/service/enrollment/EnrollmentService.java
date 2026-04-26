package com.liveklass.service.enrollment;

import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.enrollment.EnrollmentStatus;
import com.liveklass.repository.enrollment.EnrollmentRepository;
import com.liveklass.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LectureService lectureService;

    @Transactional
    public Long enroll(final Long memberId, final Long lectureId) {
        lectureService.occupySlot(lectureId);

        Enrollment enrollment = Enrollment.builder()
                .memberId(memberId)
                .lectureId(lectureId)
                .status(EnrollmentStatus.PENDING)
                .enrolledAt(LocalDateTime.now())
                .build();

        return enrollmentRepository.save(enrollment).getId();
    }

    @Transactional
    public void confirmPayment(final Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수강 신청 내역입니다."));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("결제 대기 상태인 경우에만 확정이 가능합니다.");
        }

        enrollment.confirm();
    }

    @Transactional
    public void cancelEnrollment(final Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 수강 신청 내역입니다."));

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 신청입니다.");
        }

        enrollment.cancel();
        lectureService.releaseSlot(enrollment.getLectureId());
    }

    public List<Enrollment> getMyEnrollments(final Long memberId) {
        return enrollmentRepository.findByMemberId(memberId);
    }
}
