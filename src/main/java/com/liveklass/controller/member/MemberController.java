package com.liveklass.controller.member;

import com.liveklass.controller.member.dto.CreatorRegisterRequest;
import com.liveklass.controller.member.dto.MemberJoinRequest;
import com.liveklass.service.member.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/join")
    public Long join(@Valid @RequestBody final MemberJoinRequest request) {
        return memberService.join(request.getEmail(), request.getName());
    }

    @PostMapping("/creator")
    public Long registerAsCreator(@Valid @RequestBody final CreatorRegisterRequest request) {
        return memberService.registerAsCreator(
                request.getMemberId(),
                request.getBio()
        );
    }
}
