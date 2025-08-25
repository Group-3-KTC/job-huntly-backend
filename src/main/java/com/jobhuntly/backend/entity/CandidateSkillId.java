package com.jobhuntly.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CandidateSkillId implements Serializable {
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "skill_id")
    private Long skillId;
}