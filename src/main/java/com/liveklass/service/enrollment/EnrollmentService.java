package com.liveklass.service.enrollment;

import com.liveklass.controller.enrollment.dto.EnrollmentResponse;
import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.enrollment.EnrollmentStatus;
import com.liveklass.domain.enrollment.Waitlist;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.exception.BusinessException;
import com.liveklass.exception.EntityNotFoundException;
import com.liveklass.exception.ErrorCode;
import com.liveklass.repository.enrollment.EnrollmentRepository;
import com.liveklass.repository.enrollment.WaitlistRepository;
import com.liveklass.repository.lecture.LectureRepository;
import com.liveklass.repository.member.MemberRepository;
import com.liveklass.service.lecture.LectureService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final WaitlistRepository waitlistRepository;
    private final MemberRepository memberRepository;
    private final LectureRepository lectureRepository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long enroll(final Long memberId, final Long lectureId) {
        validateMember(memberId);

        int updated = lectureRepository.incrementEnrollmentIfPossible(lectureId);

        if (updated == 0) {
            return handleFailedOccupy(memberId, lectureId);
        }

        try {
            return saveEnrollment(memberId, lectureId, EnrollmentStatus.PENDING).getId();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("이미 수강 신청된 강의입니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private Long handleFailedOccupy(Long memberId, Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.LECTURE_NOT_FOUND));

        lecture.validateOccupancy();

        return addToWaitlist(memberId, lectureId);
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
        if (enrollmentRepository.findByMemberIdAndLectureIdAndStatusNot(
                memberId, lectureId, EnrollmentStatus.CANCELLED).isPresent()) {
            throw new BusinessException("이미 수강 신청된 강의입니다.", ErrorCode.INVALID_INPUT_VALUE);
        }

        if (waitlistRepository.existsByMemberIdAndLectureId(memberId, lectureId)) {
            throw new BusinessException("이미 대기열에 등록된 강의입니다.", ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            Waitlist waitlist = new Waitlist(memberId, lectureId);
            return waitlistRepository.save(waitlist).getId();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("이미 대기열에 등록된 강의입니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
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
            int updatedCount = lectureRepository.decrementEnrollmentIfPossible(enrollment.getLectureId());

            if (updatedCount == 0) {
                Lecture lecture = lectureRepository.findById(enrollment.getLectureId())
                        .orElseThrow(() -> new EntityNotFoundException(ErrorCode.LECTURE_NOT_FOUND));
                lecture.validateRelease();
            }
        }
    }

    private void promoteFromWaitlist(Waitlist waitlist) {
        saveEnrollment(waitlist.getMemberId(), waitlist.getLectureId(), EnrollmentStatus.PENDING);
        waitlistRepository.delete(waitlist);
    }

    public Page<EnrollmentResponse> getMyEnrollments(final Long memberId, final Pageable pageable) {
        validateMember(memberId);
        return enrollmentRepository.findByMemberId(memberId, pageable)
                .map(EnrollmentResponse::new);
    }

    private void validateMember(final Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
}
