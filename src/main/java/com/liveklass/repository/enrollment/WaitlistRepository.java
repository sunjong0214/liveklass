package com.liveklass.repository.enrollment;

import com.liveklass.domain.enrollment.Waitlist;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Waitlist w WHERE w.lectureId = :lectureId " +
            "ORDER BY w.createdAt ASC LIMIT 1")
    Optional<Waitlist> findFirstByLectureIdWithLock(Long lectureId);

    boolean existsByMemberIdAndLectureId(Long memberId, Long lectureId);
}
