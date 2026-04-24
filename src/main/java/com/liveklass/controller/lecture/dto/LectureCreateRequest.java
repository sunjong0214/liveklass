package com.liveklass.controller.lecture.dto;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LectureCreateRequest {
	private String title;
	private String description;
	private Long price;
	private Integer maxCapacity;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
}
