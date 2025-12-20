package com.oscc.skillexchange.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "OTP is required")
    @Min(value = 100000, message = "Invalid OTP")
    @Max(value = 999999, message = "Invalid OTP")
    private Integer otp;

    private SignupRequest signupData;
}
