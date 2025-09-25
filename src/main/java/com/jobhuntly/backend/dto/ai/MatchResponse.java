package com.jobhuntly.backend.dto.ai;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchResponse {
    private int score;
    private List<String> reasons;
}