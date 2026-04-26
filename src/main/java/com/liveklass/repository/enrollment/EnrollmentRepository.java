package com.liveklass.repository.enrollment;

import com.liveklass.domain.enrollment.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Page<Enrollment> findByMemberId(Long memberId, Pageable pageable);
    Page<Enrollment> findByLectureId(Long lectureId, Pageable pageable);
}
