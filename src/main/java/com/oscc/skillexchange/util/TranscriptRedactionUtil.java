package com.oscc.skillexchange.util;

import java.util.regex.Pattern;

/**
 * Utility for redacting sensitive information from transcripts
 * Strips emails and phone numbers before sending to OpenAI
 */
public class TranscriptRedactionUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b(\\+?\\d{1,3}[-.]?)?\\(?\\d{3}\\)?[-.]?\\d{3}[-.]?\\d{4}\\b"
    );

    /**
     * Redact sensitive information from transcript
     */
    public static String redact(String transcript) {
        if (transcript == null || transcript.isEmpty()) {
            return transcript;
        }

        String redacted = transcript;

        // Redact emails
        redacted = EMAIL_PATTERN.matcher(redacted).replaceAll("[EMAIL_REDACTED]");

        // Redact phone numbers
        redacted = PHONE_PATTERN.matcher(redacted).replaceAll("[PHONE_REDACTED]");

        return redacted;
    }
}
