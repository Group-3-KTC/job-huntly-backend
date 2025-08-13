package com.jobhuntly.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CityRequest {
    @JsonProperty("city_name")
    private String name;
}
