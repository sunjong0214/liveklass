package com.liveklass.repository.lecture;

import com.liveklass.domain.lecture.LectureStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LectureCapacity {
    private final LectureStatus status;
    private final Integer maxCapacity;
    private final Integer currentEnrollmentCount;

    public boolean isEnrollable() {
        return this.status == LectureStatus.OPEN
                && this.currentEnrollmentCount < this.maxCapacity;
    }
}
