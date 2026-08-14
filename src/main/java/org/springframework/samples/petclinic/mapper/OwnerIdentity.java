package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Identity-key helpers shared by the owner mapper and the owner create endpoint: the SHA-256 hex digest
 * and the American Soundex code used to build and compare an owner's duplicate-detection identity key.
 *
 * <p>These live in a plain utility class rather than as default methods on {@link OwnerMapper} so MapStruct
 * does not mistake their {@code String -> String} shape for candidate property mapping methods.
 */
public final class OwnerIdentity {

    /**
     * Soundex code table for the 26 letters {@code A-Z}, where {@code '0'} marks a letter that is not
     * coded (the vowels {@code A E I O U}, plus {@code H W Y}).
     */
    private static final String SOUNDEX_MAPPING = "01230120022455012623010202";

    private OwnerIdentity() {
    }

    /**
     * Computes the lower-case SHA-256 hex digest (64 hex characters) of the given input's UTF-8 bytes.
     */
    public static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /**
     * Computes the American Soundex code of a name: the (upper-cased) first letter followed by three
     * digits derived from the remaining consonants. Adjacent letters mapping to the same digit are
     * coded once; {@code H} and {@code W} do not break such a run while a vowel does. The result is
     * right-padded with {@code '0'} to, or truncated to, four characters. Non-letter characters are
     * ignored, and a {@code null} or letterless value yields the empty string.
     */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        String letters = value.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        char first = letters.charAt(0);
        code.append(first);
        char previous = SOUNDEX_MAPPING.charAt(first - 'A');
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char letter = letters.charAt(i);
            char digit = SOUNDEX_MAPPING.charAt(letter - 'A');
            if (letter == 'H' || letter == 'W') {
                continue;
            }
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            previous = (letter == 'A' || letter == 'E' || letter == 'I'
                || letter == 'O' || letter == 'U' || letter == 'Y') ? '0' : digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }
}
