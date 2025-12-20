package com.oscc.skillexchange.exception;

import org.springframework.http.HttpStatus;

public class InvalidOtpException extends BusinessException {
    public InvalidOtpException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

