package com.liveklass.domain.enrollment;

import java.time.LocalDateTime;

import com.liveklass.domain.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_enrollment_member_lecture",
			columnNames = {"memberId", "lectureId"}
		)
	}
)
public class Enrollment extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long memberId;

	@Column(nullable = false)
	private Long lectureId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EnrollmentStatus status;

	private LocalDateTime enrolledAt;

	private LocalDateTime paymentAt;

	public void cancel() {
		if (this.status == EnrollmentStatus.CANCELLED) {
			throw new IllegalStateException("이미 취소된 신청입니다.");
		}
		if (!canCancel()) {
			throw new IllegalStateException("결제 후 7일이 경과하여 취소가 불가능합니다.");
		}
		this.status = EnrollmentStatus.CANCELLED;
	}

	public void confirm() {
		if (this.status != EnrollmentStatus.PENDING) {
			throw new IllegalStateException("결제 대기 상태인 경우에만 확정이 가능합니다.");
		}
		this.status = EnrollmentStatus.CONFIRMED;
		this.paymentAt = LocalDateTime.now();
	}

	private boolean canCancel() {
		if (this.paymentAt == null) {
			return true;
		}
		return LocalDateTime.now().isBefore(this.paymentAt.plusDays(7));
	}
}
