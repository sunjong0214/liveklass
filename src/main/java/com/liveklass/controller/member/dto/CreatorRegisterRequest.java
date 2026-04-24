package com.liveklass.controller.member.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreatorRegisterRequest {
	private final Long memberId;
	private final String bio;
}
