package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Shared identity-key derivation used by both the owner mapper (to expose {@code identityKey} on the
 * DTO) and the create endpoint (to detect duplicates). The identity key is the full lower-case hex
 * SHA-256 over {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
 *
 * <p>Kept as a plain utility class (rather than default methods on the mapper) so MapStruct does not
 * mistake these {@code String -> String} helpers for candidate property mapping methods.
 */
public final class IdentityKeys {

    /**
     * The fixed version tag mixed into every version-2 identifier so that no value produced under the
     * version-1 algorithm is ever produced again.
     */
    public static final String VERSION_TAG = "V2";

    private IdentityKeys() {
    }

    /**
     * Derive the version-2 identity key into which all duplicate detection is consolidated: the full
     * lower-case hex SHA-256 over the fixed {@code 'V2'} version tag and
     * {@code (telephone or empty) + '|' + lowerEmail + '|' + soundex(lastName)}. Mixing in the version
     * tag guarantees the key differs from the version-1 key for the same owner. A null email or last
     * name contributes an empty segment.
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String raw = VERSION_TAG + "|"
            + (telephone == null ? "" : telephone)
            + "|" + (email == null ? "" : email.toLowerCase())
            + "|" + soundex(lastName);
        return sha256Hex(raw);
    }

    /**
     * American Soundex code of {@code value}: the retained first letter followed by up to three
     * digits, zero-padded to length four. Letters map to digits (b,f,p,v-&gt;1; c,g,j,k,q,s,x,z-&gt;2;
     * d,t-&gt;3; l-&gt;4; m,n-&gt;5; r-&gt;6); adjacent letters with the same code (and letters
     * separated only by h or w) collapse to one digit, while a vowel between them keeps both. Returns
     * an empty string when {@code value} contains no letters.
     */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (char c : value.toUpperCase().toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                letters.append(c);
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char prev = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = soundexDigit(c);
            if (digit != '0' && digit != prev) {
                code.append(digit);
            }
            prev = (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y') ? '0' : digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.substring(0, 4);
    }

    /**
     * Map a single upper-case letter to its Soundex digit, or {@code '0'} for letters that are not
     * coded (a, e, i, o, u, y, h, w).
     */
    private static char soundexDigit(char c) {
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

    /**
     * Full lower-case hex SHA-256 of the UTF-8 bytes of {@code input}.
     */
    private static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
