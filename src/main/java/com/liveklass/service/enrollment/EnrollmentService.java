package com.liveklass.service.enrollment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.enrollment.EnrollmentStatus;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.repository.enrollment.EnrollmentRepository;
import com.liveklass.repository.lecture.LectureRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EnrollmentService {

	private final EnrollmentRepository enrollmentRepository;
	private final LectureRepository lectureRepository;

	@Transactional
	public Long enroll(final Long memberId, final Long lectureId) {
		Lecture lecture = lectureRepository.findById(lectureId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

		if (!lecture.isEnrollable()) {
			throw new IllegalStateException("수강 신청이 불가능한 강의이거나 정원이 초과되었습니다.");
		}

		lecture.incrementEnrollment();

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

		Lecture lecture = lectureRepository.findById(enrollment.getLectureId())
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
		lecture.decrementEnrollment();
	}

	public List<Enrollment> getMyEnrollments(final Long memberId) {
		return enrollmentRepository.findByMemberId(memberId);
	}
}
