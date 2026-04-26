package com.liveklass.service.lecture;

import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.controller.lecture.dto.LectureDetailResponse;
import com.liveklass.controller.lecture.dto.LectureResponse;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.repository.lecture.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;

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

    public Page<LectureResponse> findLectures(final LectureStatus status, final Pageable pageable) {
        if (status == null) {
            return lectureRepository.findAll(pageable).map(LectureResponse::new);
        }
        return lectureRepository.findByStatus(status, pageable).map(LectureResponse::new);
    }

    public LectureDetailResponse getLecture(final Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
        return new LectureDetailResponse(lecture);
    }

    @Transactional
    public void occupySlot(final Long lectureId) {
        int updatedCount = lectureRepository.incrementEnrollmentIfPossible(lectureId);

        if (updatedCount == 0) {
            Lecture lecture = lectureRepository.findById(lectureId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
            lecture.validateOccupancy();
        }
    }

    @Transactional
    public void releaseSlot(final Long lectureId) {
        int updatedCount = lectureRepository.decrementEnrollmentIfPossible(lectureId);

        if (updatedCount == 0) {
            Lecture lecture = lectureRepository.findById(lectureId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
            lecture.validateRelease();
        }
    }
}
