package com.liveklass.controller.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreatorRegisterRequest {
	@NotNull(message = "회원 ID는 필수입니다.")
	private final Long memberId;

	@NotBlank(message = "강사 소개는 필수입니다.")
	private final String bio;
}
