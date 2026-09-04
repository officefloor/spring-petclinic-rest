package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * The single source of truth for an owner's derived {@code identityKey} and its parts. All duplicate
 * detection is now expressed through this one key:
 *
 * <pre>identityKey = SHA-256 hex over (normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName))</pre>
 *
 * <p>Two owners are duplicates only when their <em>whole</em> identityKey is equal. Because the
 * telephone is part of the key, two owners with the same last name (and postcode) but different
 * telephones have different identityKeys and are both allowed; only an exact full-key match is a
 * duplicate. Postcode is <em>not</em> part of the key, so the household id derived from (lastName,
 * postcode) no longer feeds duplicate detection — it survives only as descriptive metadata (see
 * {@link #deriveHouseholdId}).
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The identityKey for a create request or an existing owner — the one derivation the duplicate
     * check applies to both sides of a comparison, so a request and an existing owner are keyed
     * identically. It is the SHA-256 hex (lower-case, 64 characters) of
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}: telephone is
     * canonicalized to E.164 and email is lower-cased so differing formats or case do not hide a match,
     * and the last name folds through Soundex so it matches phonetically.
     */
    public static String key(String telephone, String email, String lastName) {
        String input = canonicalTelephone(telephone) + '|' + normalizedEmail(email) + '|'
                + soundex(lastName);
        return sha256HexLower(input);
    }

    /**
     * The E.164 form used for comparison. A value that is already E.164 is returned unchanged; a value
     * that predates E.164 storage is normalized on the fly, falling back to its bare digits if it
     * cannot form valid E.164.
     */
    public static String canonicalTelephone(String value) {
        try {
            return OwnerTelephone.toE164(value);
        }
        catch (InvalidTelephoneException ex) {
            return value == null ? "" : value.replaceAll("\\D", "");
        }
    }

    /** The email lower-cased and trimmed, or the empty string when null or blank. */
    public static String normalizedEmail(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    /** Lower-cased, trimmed, with runs of whitespace collapsed to a single space. */
    public static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * The deterministic household id: the first 12 hex characters of SHA-256 over
     * {@code normalizedLastName + '|' + postcode}. Owners with the same normalized lastName and
     * postcode therefore resolve to the same value automatically, so the household is keyed on
     * (lastName, postcode). A null postcode is treated as the empty string.
     */
    public static String deriveHouseholdId(String normalizedLastName, String postcode) {
        return sha256Hex(normalizedLastName + '|' + (postcode == null ? "" : postcode), 12);
    }

    /**
     * The household id for a raw (un-normalized) {@code lastName} and {@code postcode} — normalizes the
     * lastName and defers to {@link #deriveHouseholdId}. This is the one derivation every household
     * check shares, so two owners resolve to the same household exactly when this returns the same
     * value.
     */
    public static String householdIdOf(String lastName, String postcode) {
        return deriveHouseholdId(normalizeName(lastName), postcode);
    }

    /**
     * HASH8: the first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone +
     * lastName}, where the telephone is canonicalized to E.164. This is the hashed component of the
     * owner's {@code memberId} ({@code <REGION><FY><HASH8><CHK>}).
     */
    public static String customerHash(String telephone, String lastName) {
        return sha256Hex(canonicalTelephone(telephone) + (lastName == null ? "" : lastName), 8);
    }

    /**
     * The Luhn check digit (0-9) over the decimal digits contained in {@code source}; any non-digit
     * character is skipped. This is the check-digit primitive the owner's {@code memberId} carries as
     * its CHK segment — computed over {@code <REGION><FY><HASH8>}.
     */
    public static int luhnCheckDigit(String source) {
        int sum = 0;
        boolean dbl = true;
        for (int i = source.length() - 1; i >= 0; i--) {
            char c = source.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (dbl) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            dbl = !dbl;
        }
        return (10 - (sum % 10)) % 10;
    }

    /**
     * The full SHA-256 over the UTF-8 bytes of {@code input} as 64 lower-case hex characters — the
     * encoding of the owner's {@code identityKey}.
     */
    static String sha256HexLower(String input) {
        StringBuilder sb = new StringBuilder(64);
        for (byte b : sha256(input)) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * The American Soundex code of {@code name}: the first letter followed by three digits, folding
     * consonants to their phonetic class so names that sound alike share a code (e.g. {@code Robert}
     * and {@code Rupert} both give {@code R163}). Non-letters are dropped first; a null or letter-less
     * name yields the empty string. Adjacent letters of the same class — and same-class letters
     * separated by {@code H} or {@code W} — are coded once; a vowel between them resets the run so both
     * are coded.
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
        char[] out = { letters.charAt(0), '0', '0', '0' };
        int count = 1;
        char last = mappingCode(letters, 0);
        for (int i = 1; i < letters.length() && count < 4; i++) {
            char mapped = mappingCode(letters, i);
            if (mapped != 0) {
                if (mapped != '0' && mapped != last) {
                    out[count++] = mapped;
                }
                last = mapped;
            }
        }
        return new String(out);
    }

    /** Soundex class digit of {@code letters[index]}, suppressed to 0 across an {@code H}/{@code W}. */
    private static char mappingCode(CharSequence letters, int index) {
        char mapped = soundexDigit(letters.charAt(index));
        if (index > 1 && mapped != '0') {
            char prev = letters.charAt(index - 1);
            if (prev == 'H' || prev == 'W') {
                char preHw = letters.charAt(index - 2);
                if (soundexDigit(preHw) == mapped || preHw == 'H' || preHw == 'W') {
                    return 0;
                }
            }
        }
        return mapped;
    }

    /** Soundex class digit ({@code '0'} for vowels and {@code H}/{@code W}/{@code Y}) of a letter. */
    private static char soundexDigit(char c) {
        // A B C D E F G H I J K L M N O P Q R S T U V W X Y Z
        return "01230120022455012623010202".charAt(c - 'A');
    }

    /**
     * The first {@code hexChars} upper-case hex characters of SHA-256 over the UTF-8 bytes of
     * {@code input} — the shared encoding behind every hash-derived identity part.
     */
    static String sha256Hex(String input, int hexChars) {
        StringBuilder sb = new StringBuilder(hexChars);
        for (byte b : sha256(input)) {
            sb.append(String.format("%02X", b));
            if (sb.length() >= hexChars) {
                break;
            }
        }
        return sb.substring(0, hexChars);
    }

    /**
     * The SHA-256 digest of the UTF-8 bytes of {@code input} — the shared primitive every hash-derived
     * identity part is encoded from.
     */
    static byte[] sha256(String input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
