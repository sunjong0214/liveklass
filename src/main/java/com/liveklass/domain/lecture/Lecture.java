package com.liveklass.domain.lecture;

import com.liveklass.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(indexes = {
    @Index(name = "idx_lecture_status_createdat", columnList = "status, createdAt, lecture_id DESC"),
    @Index(name = "idx_lecture_createdat", columnList = "createdAt DESC, lecture_id DESC")
})public class Lecture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    @Column(nullable = false)
    private Long creatorId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "BLOB")
    private String description;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer maxCapacity;

    private Integer currentEnrollmentCount = 0;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LectureStatus status;

    public void updateStatus(LectureStatus status) {
        this.status = status;
    }

    public void validateOccupancy() {
        if (this.status != LectureStatus.OPEN) {
            throw new IllegalStateException("수강 신청이 불가능한 강의 상태입니다.");
        }
        if (this.currentEnrollmentCount >= this.maxCapacity) {
            throw new IllegalStateException("수강 정원이 초과되었습니다.");
        }
    }

    public void validateRelease() {
        if (this.currentEnrollmentCount <= 0) {
            throw new IllegalStateException("취소할 수강 내역이 없습니다.");
        }
    }

    public void decrementEnrollment() {
        if (currentEnrollmentCount > 0) {
            this.currentEnrollmentCount--;
        }
    }
}
