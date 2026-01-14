package com.oscc.skillexchange.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillUpdateRequest {

    @NotBlank(message = "Skill must not be blank")
    private String skill;
}


