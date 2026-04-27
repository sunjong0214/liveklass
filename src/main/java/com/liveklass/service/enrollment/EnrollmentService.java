package com.liveklass.service.enrollment;

import com.liveklass.controller.enrollment.dto.EnrollmentResponse;
import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.enrollment.EnrollmentStatus;
import com.liveklass.domain.enrollment.Waitlist;
import com.liveklass.exception.BusinessException;
import com.liveklass.exception.EntityNotFoundException;
import com.liveklass.exception.ErrorCode;
import com.liveklass.repository.enrollment.EnrollmentRepository;
import com.liveklass.repository.enrollment.WaitlistRepository;
import com.liveklass.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final WaitlistRepository waitlistRepository;
    private final LectureService lectureService;

    @Transactional
    public Long enroll(final Long memberId, final Long lectureId) {
        try {
            lectureService.occupySlot(lectureId);
            return saveEnrollment(memberId, lectureId, EnrollmentStatus.PENDING).getId();
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.LECTURE_CAPACITY_EXCEEDED) {
                return addToWaitlist(memberId, lectureId);
            }
            throw e;
        }
    }

    private Enrollment saveEnrollment(Long memberId, Long lectureId, EnrollmentStatus status) {
        Enrollment enrollment = Enrollment.builder()
                .memberId(memberId)
                .lectureId(lectureId)
                .status(status)
                .enrolledAt(LocalDateTime.now())
                .build();
        return enrollmentRepository.save(enrollment);
    }

    private Long addToWaitlist(Long memberId, Long lectureId) {
        if (waitlistRepository.existsByMemberIdAndLectureId(memberId, lectureId)) {
            throw new BusinessException("이미 대기열에 등록된 강의입니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
        Waitlist waitlist = new Waitlist(memberId, lectureId);
        return waitlistRepository.save(waitlist).getId();
    }

    @Transactional
    public void confirmPayment(final Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENROLLMENT_NOT_FOUND));
        enrollment.confirm();
    }

    @Transactional
    public void cancelEnrollment(final Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENROLLMENT_NOT_FOUND));

        enrollment.cancel();

        Optional<Waitlist> firstWaiting = waitlistRepository.findFirstByLectureIdWithLock(enrollment.getLectureId());

        if (firstWaiting.isPresent()) {
            promoteFromWaitlist(firstWaiting.get());
        } else {
            lectureService.releaseSlot(enrollment.getLectureId());
        }
    }

    private void promoteFromWaitlist(Waitlist waitlist) {
        saveEnrollment(waitlist.getMemberId(), waitlist.getLectureId(), EnrollmentStatus.PENDING);
        waitlistRepository.delete(waitlist);
    }

    public Page<EnrollmentResponse> getMyEnrollments(final Long memberId, final Pageable pageable) {
        return enrollmentRepository.findByMemberId(memberId, pageable)
                .map(EnrollmentResponse::new);
    }
}
