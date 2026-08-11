package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Derives an owner's {@code identityKey} &mdash; the single value all duplicate detection is now
 * expressed through.
 *
 * <p>The key is the lower-case hex SHA-256 of
 * {@code normalizedTelephone + '|' + (email or empty, lower-cased) + '|' + soundex(lastName)}.
 * Two owners are duplicates only when their <em>whole</em> keys are equal: because the telephone is
 * part of the hashed input, two owners sharing a last name and postcode but with different
 * telephones have different keys and are both allowed &mdash; the second is recorded as a soft match
 * rather than rejected.
 */
public final class OwnerIdentity {

    /**
     * The fixed version tag folded into the region code used inside every version-2 identifier. It
     * lives only inside the identifiers (member id, identity key and household id): mixing it in
     * guarantees every identifier differs from its version-1 value and never reproduces one, while
     * the plain region &mdash; the user-facing {@code locality}, {@code timezone} and the owner
     * segment's derived region &mdash; is left untouched.
     */
    public static final String VERSION_TAG = "V2";

    private OwnerIdentity() {
    }

    /**
     * The region code mixed inside the version-2 identifiers: the plain region (e.g. {@code "NSW"})
     * with the fixed {@link #VERSION_TAG} folded in (e.g. {@code "NSWV2"}). It is a hashed ingredient
     * of the member id, identity key and household id only, so it never surfaces in {@code locality},
     * {@code timezone} or the owner segment's region, which stay the plain region code.
     */
    public static String regionCode(String region) {
        return (region == null ? "" : region) + VERSION_TAG;
    }

    /**
     * Builds the identity key from an owner's parts: the full lower-case hex SHA-256 of
     * {@code regionCode + '|' + normalizedTelephone + '|' + email + '|' + soundex(lastName)}, where
     * {@code regionCode} is the version-2 region code (see {@link #regionCode(String)}) that folds in
     * the {@code 'V2'} version tag. The telephone is expected to already be in E.164 form (as stored);
     * the email is lower-cased and a null/blank email contributes an empty segment; the last name
     * contributes its Soundex code.
     */
    public static String key(String region, String telephone, String email, String lastName) {
        String tel = telephone == null ? "" : telephone;
        String em = (email == null || email.isBlank()) ? "" : email.trim().toLowerCase(Locale.ROOT);
        String raw = regionCode(region) + "|" + tel + "|" + em + "|" + soundex(lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * American Soundex of a name: the first letter (upper-cased) followed by three digits encoding
     * the remaining consonant sounds, zero-padded to length four. Adjacent letters coding to the
     * same digit collapse to one; vowels (and {@code y}) reset the run while {@code h} and {@code w}
     * are transparent. Returns {@code ""} for a null or letter-free value.
     */
    public static String soundex(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prevDigit = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            char digit = soundexDigit(c);
            if (digit != '0' && digit != prevDigit) {
                code.append(digit);
            }
            if (c != 'H' && c != 'W') {
                prevDigit = digit; // vowels reset the run; H and W are transparent
            }
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    private static char soundexDigit(char c) {
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

    /**
     * The HASH8 segment of an owner's {@code '<REGION><FY><HASH8><CHK>'} member id: the first 8
     * upper-case hex characters of SHA-256 over {@code regionCode + normalizedTelephone + lastName},
     * where {@code regionCode} is the version-2 region code (see {@link #regionCode(String)}) folding
     * in the {@code 'V2'} version tag. The tag lives inside the hash only; the member id's visible
     * REGION prefix stays the plain region, so {@code locality} and the owner segment are unaffected.
     * The telephone is expected to already be in E.164 form (as stored).
     */
    public static String customerHash(String region, String normalizedTelephone, String lastName) {
        String tel = normalizedTelephone == null ? "" : normalizedTelephone;
        String last = lastName == null ? "" : lastName;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((regionCode(region) + tel + last).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * The household's stable, deterministic identifier: the first 12 upper-case hex characters of
     * SHA-256 over {@code regionCode + '|' + normalizedLastName + '|' + postcode}, where
     * {@code regionCode} is the version-2 region code (see {@link #regionCode(String)}) folding in the
     * {@code 'V2'} version tag. Owners sharing a last name and postcode therefore resolve to the same
     * value automatically (they share a region too), regardless of creation order or whether they
     * opted into {@code sharesHousehold}. Returns {@code null} when no postcode is given, since a
     * household is keyed on (last name, postcode) and cannot form without one.
     */
    public static String householdId(String region, String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        try {
            String key = regionCode(region) + "|" + normalizeName(lastName) + "|" + postcode;
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Lower-case, trim, and collapse internal whitespace runs to a single space. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
