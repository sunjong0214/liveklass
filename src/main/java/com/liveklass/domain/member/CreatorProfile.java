package com.liveklass.domain.member;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CreatorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "creator_profile_id")
    private Long id;

    @NotNull
    @Column(nullable = false, unique = true)
    private Long memberId;

    @NotBlank
    @Lob
    @Column(nullable = false, columnDefinition = "BLOB")
    private String bio;

    public CreatorProfile(final Long memberId, final String bio) {
        this.memberId = memberId;
        this.bio = bio;
    }
}
