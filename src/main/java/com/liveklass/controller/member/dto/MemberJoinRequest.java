package com.liveklass.controller.member.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class MemberJoinRequest {
    private final String email;
    private final String name;
}
