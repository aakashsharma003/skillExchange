package com.oscc.skillexchange.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequestDto {

    @NotBlank(message = "Receiver ID is required")
    private String receiverId;

    @NotBlank(message = "Requested skill is required")
    private String requestedSkill;

    private String offeredSkill;

    @Size(max = 500)
    private String message;
}
