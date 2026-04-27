package com.liveklass.controller.lecture.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LectureCreateRequest {
    @NotBlank(message = "강의 제목은 필수입니다.")
    private String title;

    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Long price;

    @NotNull(message = "정원은 필수입니다.")
    @Min(value = 1, message = "정원은 최소 1명 이상이어야 합니다.")
    private Integer maxCapacity;

    @NotNull(message = "강의 시작일은 필수입니다.")
    private LocalDateTime startDate;

    @NotNull(message = "강의 종료일은 필수입니다.")
    private LocalDateTime endDate;
}
