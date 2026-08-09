package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives an owner's {@code identityKey}, the single value used for duplicate detection. The
 * key is the full lower-case hex SHA-256 digest over
 * {@code '<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>'}. Two owners are duplicates
 * exactly when their identity keys are equal.
 *
 * <p>Kept out of {@link OwnerMapper} so MapStruct does not mistake its {@code String -> String}
 * methods for property mappings and apply them to every string property.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Builds the duplicate-detection identity key from an owner's normalized telephone, email
     * and last name. The email is trimmed and lower-cased and the last name is reduced to its
     * {@link #soundex soundex} code before hashing; a null telephone, email or last name
     * contributes an empty component. The result is the full lower-case hex SHA-256 of
     * {@code '<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>'}.
     *
     * @param normalizedTelephone the E.164 telephone (may be {@code null})
     * @param email               the owner's email, lower-cased here (may be {@code null})
     * @param lastName            the owner's last name, reduced to its soundex code (may be {@code null})
     * @return the 64-character lower-case hex identity key
     */
    public static String identityKey(String normalizedTelephone, String email, String lastName) {
        String telephone = normalizedTelephone == null ? "" : normalizedTelephone;
        String lowerEmail = email == null ? "" : email.trim().toLowerCase();
        String source = telephone + "|" + lowerEmail + "|" + soundex(lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Computes the American Soundex code of {@code name}: the upper-cased first letter followed
     * by three digits derived from the remaining consonants (vowels and the letters {@code h}
     * and {@code w} act as described by the standard algorithm), padded with zeros or truncated
     * to length four. Non-letters are ignored. Returns an empty string when {@code name} is null
     * or contains no letters.
     *
     * @param name the value to encode
     * @return the four-character soundex code, or an empty string when there is nothing to encode
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previousDigit = digit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            // 'H' and 'W' are transparent: two consonants separated by them are treated as
            // adjacent, so the previous digit is carried across without being emitted again.
            if (c == 'H' || c == 'W') {
                continue;
            }
            char d = digit(c);
            if (d != '0' && d != previousDigit) {
                code.append(d);
            }
            // Vowels reset the previous digit to '0', so a repeated digit that a vowel separates
            // is emitted twice.
            previousDigit = d;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /**
     * Maps a letter to its soundex digit; vowels (and the transparent {@code h}/{@code w}) map
     * to {@code '0'}.
     */
    private static char digit(char c) {
        return switch (c) {
            case 'B', 'F', 'P', 'V' -> '1';
            case 'C', 'G', 'J', 'K', 'Q', 'S', 'X', 'Z' -> '2';
            case 'D', 'T' -> '3';
            case 'L' -> '4';
            case 'M', 'N' -> '5';
            case 'R' -> '6';
            default -> '0';
        };
    }
}
