package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}: the single value all duplicate detection is expressed
 * through. It is the SHA-256, rendered as 64 lower-case hex characters, of
 * {@code 'V2' + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)} — the
 * version-2 derivation that mixes in the fixed {@code 'V2'} tag, where the email segment
 * is empty when the owner has none. Two owners are the same identity when their whole identityKey is
 * equal; because the telephone is part of the key, two people with the same last name (same soundex)
 * and postcode but <em>different</em> telephones have different keys and are both allowed — they are
 * a soft match, not a hard duplicate.
 *
 * <p>The telephone is normalized to E.164 (the same way as {@link E164Telephone}) and the email is
 * trimmed and lower-cased, so differently-formatted values still produce the same key. The last name
 * is reduced to its {@link #soundex(String) Soundex} code, so phonetically-equal surnames share the
 * same key segment. The key is computed identically for a not-yet-saved owner (in
 * {@link CheckOwnerIdentityUnique}) and for the response, so what a client reads back is exactly what
 * collisions are judged on.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /**
     * The owner's identity key: the 64-hex SHA-256 of
     * {@code 'V2'|telephone|email|soundex(lastName)}. The fixed {@link OwnerRegion#VERSION_TAG 'V2'}
     * version tag is mixed into the hashed input, so every v2 identityKey differs from the v1 value
     * for the same owner while equality (what duplicate detection keys off) is preserved.
     */
    public static String of(Owner owner) {
        String raw = OwnerRegion.VERSION_TAG + "|" + normalizeTelephone(owner.getTelephone()) + "|"
                + normalizeEmail(owner.getEmail()) + "|" + soundex(owner.getLastName());
        return sha256Hex(raw);
    }

    private static String normalizeTelephone(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = E164Telephone.normalize(raw);
        return normalized != null ? normalized : raw.trim();
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? "" : trimmed.toLowerCase(Locale.ROOT);
    }

    /** Lower-case hex SHA-256 digest of the given value. */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
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

    /**
     * The American Soundex code of a name: the first letter followed by three digits, so that
     * phonetically-similar surnames share a code. A {@code null}, empty, or letter-free value yields
     * the empty string.
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
        char previous = digit(first);
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char d = digit(c);
            if (d != '0' && d != previous) {
                code.append(d);
            }
            // 'H' and 'W' do not reset the running code (so consonants they separate still merge);
            // every other letter, vowels included, does.
            if (c != 'H' && c != 'W') {
                previous = d;
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** Soundex digit for a letter; {@code '0'} for vowels and the ignored letters (H, W, Y). */
    private static char digit(char c) {
        switch (c) {
            case 'B': case 'F': case 'P': case 'V':
                return '1';
            case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z':
                return '2';
            case 'D': case 'T':
                return '3';
            case 'L':
                return '4';
            case 'M': case 'N':
                return '5';
            case 'R':
                return '6';
            default:
                return '0';
        }
    }
}
