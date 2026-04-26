package com.liveklass.service.lecture;

import com.liveklass.controller.dto.CursorPageResponse;
import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.controller.lecture.dto.LectureDetailResponse;
import com.liveklass.controller.lecture.dto.LectureResponse;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.repository.lecture.LectureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    public CursorPageResponse<LectureResponse> findLectures(
            final LectureStatus status,
            final LocalDateTime lastCreatedAt,
            final Long lastId,
            final int size) {

        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<Lecture> lectures = null;
        if (status == null) {
            lectures = lectureRepository.findLecturesWithoutStatus(lastCreatedAt, lastId, pageRequest);
        } else {
            lectures = lectureRepository.findLectures(status, lastCreatedAt, lastId, pageRequest);
        }

        boolean hasNext = lectures.size() > size;
        List<Lecture> content = hasNext ? lectures.subList(0, size) : lectures;

        LocalDateTime nextCursorCreatedAt = null;
        Long nextCursorId = null;

        if (!content.isEmpty()) {
            Lecture lastLecture = content.get(content.size() - 1);
            nextCursorCreatedAt = lastLecture.getCreatedAt();
            nextCursorId = lastLecture.getId();
        }

        List<LectureResponse> responses = content.stream()
                .map(LectureResponse::new)
                .collect(Collectors.toList());

        return new CursorPageResponse<>(responses, nextCursorCreatedAt, nextCursorId, hasNext);
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
