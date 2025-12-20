package com.oscc.skillexchange.exception;

import org.springframework.http.HttpStatus;

public class AccountLockedException extends BusinessException {
    public AccountLockedException() {
        super("Account is locked. Please contact support.", HttpStatus.LOCKED);
    }
}
