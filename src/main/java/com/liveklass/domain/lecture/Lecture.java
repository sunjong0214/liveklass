package com.liveklass.domain.lecture;

import java.time.LocalDateTime;

import com.liveklass.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Lecture extends BaseEntity {

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

	@Version
	private Long version;

	public void updateStatus(LectureStatus status) {
		this.status = status;
	}

	public boolean isEnrollable() {
		return this.status == LectureStatus.OPEN && currentEnrollmentCount < maxCapacity;
	}

	public void incrementEnrollment() {
		if (currentEnrollmentCount >= maxCapacity) {
			throw new IllegalStateException("정원이 초과되었습니다.");
		}
		this.currentEnrollmentCount++;
	}

	public void decrementEnrollment() {
		if (currentEnrollmentCount > 0) {
			this.currentEnrollmentCount--;
		}
	}
}
