package com.liveklass.repository.lecture;

import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    @Query("SELECT l FROM Lecture l " +
            "WHERE l.status = :status " +
            "AND (:lastCreatedAt IS NULL OR l.createdAt < :lastCreatedAt OR (l.createdAt = :lastCreatedAt AND l.id < :lastId)) " +
            "ORDER BY l.createdAt DESC, l.id DESC")
    List<Lecture> findLectures(
            @Param("status") LectureStatus status,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastId") Long lastId,
            Pageable pageable);

    @Query("SELECT l FROM Lecture l " +
            "WHERE (:lastCreatedAt IS NULL OR l.createdAt < :lastCreatedAt OR (l.createdAt = :lastCreatedAt AND l.id < :lastId)) " +
            "ORDER BY l.createdAt DESC, l.id DESC")
    List<Lecture> findLecturesWithoutStatus(
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastId") Long lastId,
            Pageable pageable);

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
