package com.jobhuntly.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CityDTO {
    @JsonProperty("city_name")
    private String name;
}
