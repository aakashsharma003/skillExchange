package com.skillexchange.model.request;

import com.skillexchange.model.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequest {

    private UserDetails userDetails;

    private int otp;
}
