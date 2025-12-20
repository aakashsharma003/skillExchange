package com.oscc.skillexchange.util;

import com.oscc.skillexchange.exception.InvalidTokenException;

public final class TokenUtil {

    private TokenUtil() {}

    public static String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
            throw new InvalidTokenException("Missing or invalid Authorization header");
        }
        return authHeader.substring(AppConstants.BEARER_PREFIX.length()).trim();
    }

    public static String extractTokenOrNull(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(AppConstants.BEARER_PREFIX)) {
            return null;
        }
        return authHeader.substring(AppConstants.BEARER_PREFIX.length()).trim();
    }
}
