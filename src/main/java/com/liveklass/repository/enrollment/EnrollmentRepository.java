package com.liveklass.repository.enrollment;

import com.liveklass.domain.enrollment.Enrollment;
import com.liveklass.domain.enrollment.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findByMemberId(Long memberId, Pageable pageable);
    Page<Enrollment> findByLectureId(Long lectureId, Pageable pageable);
    
    Optional<Enrollment> findByMemberIdAndLectureIdAndStatusNot(Long memberId, Long lectureId, EnrollmentStatus status);
}
