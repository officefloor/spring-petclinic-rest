package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives an owner's identity key, the single value used for duplicate detection.
 *
 * <p>The key is the lower-case SHA-256 hex digest (64 characters) over
 * {@code '<normalizedTelephone>|<lowerEmail>|<soundex(lastName)>'}, built from the
 * owner's normalized E.164 telephone, lower-cased email and the Soundex code of the
 * last name. A {@code null} telephone or email contributes an empty string, so the
 * pre-hash input always has the same {@code a|b|c} shape. Two owners are duplicates
 * only when their whole keys are equal.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic {@code String -> String} mapping
 * method and apply it to unrelated fields; the mapper references it only through
 * an explicit expression, and the create endpoint uses it for the duplicate check.
 */
public final class IdentityKeyDeriver {

    /**
     * American Soundex letter-to-digit mapping for A..Z. {@code '0'} marks a
     * non-coded letter (a vowel, plus H, W and Y).
     */
    private static final char[] SOUNDEX_MAP = "01230120022455012623010202".toCharArray();

    private IdentityKeyDeriver() {
    }

    /**
     * Returns the identity key: the lower-case SHA-256 hex digest over
     * {@code '<telephone>|<email>|<soundex(lastName)>'}, treating a {@code null}
     * telephone or email as the empty string.
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String raw = (telephone == null ? "" : telephone)
            + "|" + (email == null ? "" : email)
            + "|" + soundex(lastName);
        return sha256Hex(raw);
    }

    /**
     * Returns the American Soundex code (an initial letter followed by three digits) of the
     * given name, comparing names by how they sound. Non-letters are ignored and case is
     * folded before coding. A {@code null} value or a value with no letters yields the empty
     * string.
     */
    public static String soundex(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder cleaned = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = Character.toUpperCase(name.charAt(i));
            if (c >= 'A' && c <= 'Z') {
                cleaned.append(c);
            }
        }
        if (cleaned.length() == 0) {
            return "";
        }
        String s = cleaned.toString();
        char[] out = {'0', '0', '0', '0'};
        out[0] = s.charAt(0);
        int count = 1;
        char last = mappingCode(s, 0);
        for (int i = 1; i < s.length() && count < out.length; i++) {
            char mapped = mappingCode(s, i);
            if (mapped != 0) {
                if (mapped != '0' && mapped != last) {
                    out[count++] = mapped;
                }
                last = mapped;
            }
        }
        return new String(out);
    }

    /** The Soundex digit for a cleaned upper-case letter. */
    private static char map(char c) {
        return SOUNDEX_MAP[c - 'A'];
    }

    /**
     * The Soundex code contributed by the letter at {@code index}, applying the H/W rule: a
     * letter separated from an equal-coded predecessor by a single H or W contributes nothing
     * (returned as {@code '\0'}) so the two are treated as one.
     */
    private static char mappingCode(String s, int index) {
        char mapped = map(s.charAt(index));
        if (index > 1 && mapped != '0') {
            char hw = s.charAt(index - 1);
            if (hw == 'H' || hw == 'W') {
                char preHW = s.charAt(index - 2);
                if (map(preHW) == mapped || preHW == 'H' || preHW == 'W') {
                    return 0;
                }
            }
        }
        return mapped;
    }

    /** The full lower-case SHA-256 hex digest (64 characters) of the UTF-8 bytes of {@code s}. */
    private static String sha256Hex(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(s.getBytes(StandardCharsets.UTF_8));
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
