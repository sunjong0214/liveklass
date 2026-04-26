package com.liveklass.service.lecture;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.repository.lecture.LectureRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LectureService {

	private final LectureRepository lectureRepository;

	/*
		todo: 덜 개발
	 */
	@Transactional
	public Long createLecture(final Long creatorId, final LectureCreateRequest request) {
		Lecture lecture = Lecture.builder()
			.creatorId(creatorId)
			.title(request.getTitle())
			.description(request.getDescription())
			.price(request.getPrice())
			.maxCapacity(request.getMaxCapacity())
			.currentEnrollmentCount(0)
			.startDate(request.getStartDate())
			.endDate(request.getEndDate())
			.status(LectureStatus.DRAFT)
			.build();

		return lectureRepository.save(lecture).getId();
	}

	/*
		todo: 덜 개발
	 */
	@Transactional
	public void updateStatus(final Long lectureId, final LectureStatus status) {
		Lecture lecture = lectureRepository.findById(lectureId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
		lecture.updateStatus(status);
	}


	/*
		todo: 덜 개발
	 */
	public List<Lecture> findLectures(final LectureStatus status) {
		if (status == null) {
			return lectureRepository.findAll();
		}
		return lectureRepository.findByStatus(status);
	}

	/*
		todo: 덜 개발
	 */
	public Lecture getLecture(final Long lectureId) {
		return lectureRepository.findById(lectureId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
	}

	@Transactional
	public void occupySlot(final Long lectureId) {
		int updatedCount = lectureRepository.incrementEnrollmentIfPossible(lectureId);

		if (updatedCount == 0) {
			validateOccupancyFailure(lectureId);
		}
	}

	@Transactional
	public void releaseSlot(final Long lectureId) {
		Lecture lecture = lectureRepository.findById(lectureId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
		lecture.decrementEnrollment();
	}

	private void validateOccupancyFailure(final Long lectureId) {
		Lecture lecture = lectureRepository.findById(lectureId)
			.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

		if (lecture.getStatus() != LectureStatus.OPEN) {
			throw new IllegalStateException("수강 신청이 불가능한 강의 상태입니다.");
		}

		if (lecture.getCurrentEnrollmentCount() >= lecture.getMaxCapacity()) {
			throw new IllegalStateException("수강 정원이 초과되었습니다.");
		}
	}
}
