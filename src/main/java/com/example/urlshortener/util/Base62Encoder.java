package com.example.urlshortener.util;

import java.security.SecureRandom;

public final class Base62Encoder {

    private static final char[] ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private Base62Encoder() {
    }

    public static String generateRandomCode(int length) {
        if (length < 6 || length > 8) {
            throw new IllegalArgumentException("Short code length must be between 6 and 8 characters");
        }
        StringBuilder builder = new StringBuilder(length);
        synchronized (RANDOM) {
            for (int i = 0; i < length; i++) {
                builder.append(ALPHABET[RANDOM.nextInt(ALPHABET.length)]);
            }
        }
        return builder.toString();
    }
}
