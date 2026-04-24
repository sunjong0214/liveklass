package com.liveklass.repository.member;

import com.liveklass.domain.member.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {
    Optional<CreatorProfile> findByMemberId(Long memberId);
}
