package com.liveklass.repository.lecture;

import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    Page<Lecture> findByStatus(LectureStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l " +
            "SET l.currentEnrollmentCount = l.currentEnrollmentCount + 1 " +
            "WHERE l.id = :id " +
            "AND l.currentEnrollmentCount < l.maxCapacity " +
            "AND l.status = com.liveklass.domain.lecture.LectureStatus.OPEN")
    int incrementEnrollmentIfPossible(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Lecture l " +
            "SET l.currentEnrollmentCount = l.currentEnrollmentCount - 1 " +
            "WHERE l.id = :id " +
            "AND l.currentEnrollmentCount > 0 " +
            "AND l.status = com.liveklass.domain.lecture.LectureStatus.OPEN")
    int decrementEnrollmentIfPossible(@Param("id") Long id);
}
