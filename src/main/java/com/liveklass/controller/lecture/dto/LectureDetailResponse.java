package com.liveklass.controller.lecture.dto;

import com.liveklass.domain.lecture.Lecture;
import com.liveklass.domain.lecture.LectureStatus;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class LectureDetailResponse {
    private final Long id;
    private final Long creatorId;
    private final String title;
    private final String description;
    private final Long price;
    private final Integer maxCapacity;
    private final Integer currentEnrollmentCount;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final LectureStatus status;
    private final LocalDateTime createdAt;

    public LectureDetailResponse(final Lecture lecture) {
        this.id = lecture.getId();
        this.creatorId = lecture.getCreatorId();
        this.title = lecture.getTitle();
        this.description = lecture.getDescription();
        this.price = lecture.getPrice();
        this.maxCapacity = lecture.getMaxCapacity();
        this.currentEnrollmentCount = lecture.getCurrentEnrollmentCount();
        this.startDate = lecture.getStartDate();
        this.endDate = lecture.getEndDate();
        this.status = lecture.getStatus();
        this.createdAt = lecture.getCreatedAt();
    }
}
