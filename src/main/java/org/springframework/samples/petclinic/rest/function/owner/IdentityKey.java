package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.apache.commons.codec.language.Soundex;

/**
 * Derives the single duplicate-detection key that consolidates the former separate
 * telephone, email and household checks. It is the SHA-256 hex over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, with an empty
 * segment for an absent value. Telephone is already E.164-normalised and email lower-cased
 * by earlier steps, so two owners collide only when their whole key matches. The
 * {@code postcode} argument is retained for the mapper's stable call site but is not part of
 * the key.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(String telephone, String email, String lastName, String postcode) {
        String raw = blankToEmpty(telephone) + '|'
                + blankToEmpty(email).toLowerCase(Locale.ROOT) + '|'
                + soundex(lastName);
        return sha256Hex(raw);
    }

    /** Soundex of {@code lastName}, letters-only so the codec never rejects the input. */
    public static String soundex(String lastName) {
        String cleaned = lastName == null ? "" : lastName.replaceAll("[^A-Za-z]", "");
        return cleaned.isEmpty() ? "" : Soundex.US_ENGLISH.soundex(cleaned);
    }

    private static String blankToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
