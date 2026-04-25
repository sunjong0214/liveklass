package com.liveklass.service.member;

import com.liveklass.domain.member.CreatorProfile;
import com.liveklass.domain.member.Member;
import com.liveklass.repository.member.CreatorProfileRepository;
import com.liveklass.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final CreatorProfileRepository creatorProfileRepository;

    @Transactional
    public Long join(final String email, final String name) {
        validateDuplicateMember(email);
        Member member = new Member(email, name);
        return memberRepository.save(member).getId();
    }

    @Transactional
    public Long registerAsCreator(final Long memberId, final String bio) {
        ensureMemberCanBeCreator(memberId);
        CreatorProfile profile = new CreatorProfile(memberId, bio);
        return creatorProfileRepository.save(profile).getId();
    }

    private void ensureMemberCanBeCreator(final Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }

        creatorProfileRepository.findByMemberId(memberId).ifPresent(p -> {
            throw new IllegalStateException("이미 강사 프로필이 존재합니다.");
        });
    }

    private void validateDuplicateMember(final String email) {
        memberRepository.findByEmail(email).ifPresent(m -> {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        });
    }
}
