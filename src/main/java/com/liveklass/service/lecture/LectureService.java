package com.liveklass.service.lecture;

import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.controller.lecture.dto.LectureDetailResponse;
import com.liveklass.controller.lecture.dto.LectureResponse;
import com.liveklass.controller.lecture.dto.LectureStudentResponse;
import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.domain.member.Member;
import com.liveklass.exception.BusinessException;
import com.liveklass.exception.EntityNotFoundException;
import com.liveklass.exception.ErrorCode;
import com.liveklass.repository.enrollment.EnrollmentRepository;
import com.liveklass.repository.lecture.LectureRepository;
import com.liveklass.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LectureService {

    private final LectureRepository lectureRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long createLecture(final Long creatorId, final LectureCreateRequest request) {
        if (!memberRepository.existsById(creatorId)) {
            throw new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        validateLectureDates(request.getStartDate(), request.getEndDate());

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
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.LECTURE_NOT_FOUND));
        return new LectureDetailResponse(lecture);
    }

    public Page<LectureStudentResponse> getLectureStudents(final Long creatorId, final Long lectureId, final Pageable pageable) {
        if (!memberRepository.existsById(creatorId)) {
            throw new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.LECTURE_NOT_FOUND));

        if (!lecture.getCreatorId().equals(creatorId)) {
            throw new BusinessException(ErrorCode.NOT_LECTURE_CREATOR);
        }

        Page<Enrollment> enrollmentPage = enrollmentRepository.findByLectureId(lectureId, pageable);
        List<Enrollment> enrollments = enrollmentPage.getContent();

        Set<Long> memberIds = enrollments.stream()
                .map(Enrollment::getMemberId)
                .collect(Collectors.toSet());

        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        List<LectureStudentResponse> responses = enrollments.stream()
                .map(enrollment -> {
                    Member member = memberMap.get(enrollment.getMemberId());
                    return new LectureStudentResponse(member, enrollment);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, enrollmentPage.getTotalElements());
    }

    private void validateLectureDates(final LocalDateTime startDate, final LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("강의 시작일은 종료일보다 빨라야 합니다.", ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
