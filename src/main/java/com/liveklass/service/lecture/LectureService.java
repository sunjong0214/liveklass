package com.liveklass.service.lecture;

import com.liveklass.controller.lecture.dto.LectureCreateRequest;
import com.liveklass.controller.lecture.dto.LectureDetailResponse;
import com.liveklass.controller.lecture.dto.LectureResponse;
import com.liveklass.controller.lecture.dto.LectureStudentResponse;
import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import com.liveklass.domain.member.Member;
import com.liveklass.repository.enrollment.EnrollmentRepository;
import com.liveklass.repository.lecture.LectureRepository;
import com.liveklass.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    /*
        todo: 코드 최적화 필요
     */
    public Page<LectureStudentResponse> getLectureStudents(final Long creatorId, final Long lectureId, final Pageable pageable) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 강의입니다."));

        if (!lecture.getCreatorId().equals(creatorId)) {
            throw new IllegalStateException("해당 강의의 크리에이터만 수강생 목록을 조회할 수 있습니다.");
        }

        Page<Enrollment> enrollmentPage = enrollmentRepository.findByLectureId(lectureId, pageable);
        List<Enrollment> enrollments = enrollmentPage.getContent();

        Set<Long> memberIds = enrollments.stream()
                .map(Enrollment::getMemberId)
                .collect(Collectors.toSet());

        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds)
                .stream()
                .collect(
                        Collectors.toMap(Member::getId, member -> member)
                );

        List<LectureStudentResponse> responses = enrollments.stream()
                .map(enrollment -> {
                    Member member = memberMap.get(enrollment.getMemberId());
                    return new LectureStudentResponse(member, enrollment);
                })
                .collect(Collectors.toList());

        return new PageImpl<>(responses, pageable, enrollmentPage.getTotalElements());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
