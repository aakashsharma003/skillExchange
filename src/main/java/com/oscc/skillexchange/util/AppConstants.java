package com.oscc.skillexchange.util;

public final class AppConstants {

    private AppConstants() {}

    // Token
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";

    // Messages
    public static final class Messages {
        public static final String USER_NOT_FOUND = "User not found";
        public static final String EMAIL_ALREADY_EXISTS = "Email already registered";
        public static final String INVALID_CREDENTIALS = "Invalid email or password";
        public static final String INVALID_TOKEN = "Invalid or expired token";
        public static final String OTP_SENT = "OTP sent successfully";
        public static final String OTP_VERIFIED = "OTP verified successfully";
        public static final String OTP_INVALID = "Invalid OTP";
        public static final String OTP_EXPIRED = "OTP has expired";
        public static final String LOGIN_SUCCESS = "Login successful";
        public static final String SIGNUP_SUCCESS = "Signup successful";
        public static final String PROFILE_UPDATED = "Profile updated successfully";
        public static final String REQUEST_CREATED = "Request created successfully";
        public static final String REQUEST_UPDATED = "Request updated successfully";
        public static final String DUPLICATE_REQUEST = "Request already exists for this skill";

        private Messages() {}
    }

    // Validation
    public static final class Validation {
        public static final int MIN_PASSWORD_LENGTH = 8;
        public static final int MAX_PASSWORD_LENGTH = 100;
        public static final int MIN_NAME_LENGTH = 2;
        public static final int MAX_NAME_LENGTH = 100;
        public static final int MAX_BIO_LENGTH = 500;
        public static final int OTP_LENGTH = 6;

        private Validation() {}
    }
}
