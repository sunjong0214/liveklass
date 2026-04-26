package com.liveklass.service.lecture;

import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.repository.lecture.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    @Transactional
    public void updateStatus(final Long lectureId, final LectureStatus status) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
        lecture.updateStatus(status);
    }

    public List<Lecture> findLectures(
            final LectureStatus status,
            final LocalDateTime lastCreatedAt,
            final Long lastId,
            final int size) {

        PageRequest pageRequest = PageRequest.of(0, size);

        if (status == null) {
            return lectureRepository.findAll(pageRequest).getContent();
        }

        return lectureRepository.findByStatusWithCursor(status, lastCreatedAt, lastId, pageRequest);
    }

    @Transactional
    public Lecture getLecture(final Long lectureId) {
        return lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));
    }

    @Transactional
    public void occupySlot(final Long lectureId) {
        int updatedCount = lectureRepository.incrementEnrollmentIfPossible(lectureId);

        if (updatedCount == 0) {
            Lecture lecture = getLecture(lectureId);
            lecture.validateOccupancy();
        }
    }

    @Transactional
    public void releaseSlot(final Long lectureId) {
        int updatedCount = lectureRepository.decrementEnrollmentIfPossible(lectureId);

        if (updatedCount == 0) {
            Lecture lecture = getLecture(lectureId);
            lecture.validateRelease();
        }
    }
}
