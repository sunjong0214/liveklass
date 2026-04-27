package com.liveklass.domain.enrollment;

import com.liveklass.domain.BaseEntity;
import com.liveklass.exception.BusinessException;
import com.liveklass.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_enrollment_member_lecture",
                        columnNames = {"member_id", "lecture_id"}
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
            throw new BusinessException(ErrorCode.ALREADY_CANCELLED);
        }
        if (!canCancel()) {
            throw new BusinessException(ErrorCode.CANCELLATION_PERIOD_EXCEEDED);
        }
        this.status = EnrollmentStatus.CANCELLED;
    }

    public void confirm() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_CONFIRMATION_STATE);
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
