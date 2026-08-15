package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Single source of truth for an owner's <em>identity</em>. All duplicate detection on the create
 * endpoint is expressed through one derived {@code identityKey}:
 *
 * <pre>identityKey = SHA-256 hex over ('V2' + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName))</pre>
 *
 * <p>Both derived identifiers are version 2: the fixed {@link MemberId#VERSION_TAG} is mixed into the
 * hashed input so every value differs from the version-1 key and no version-1 value recurs.</p>
 *
 * <p>The result is the full 64-character lower-case hex SHA-256 digest. A new owner is a duplicate
 * (409) only when its whole {@code identityKey} equals an existing owner's — see
 * {@link CheckIdentityUnique}. Because the normalized telephone is part of the hashed input, two
 * members of the same household (same last name and postcode) with different telephones have
 * different keys and are both allowed; only an exact full-key match collides.
 *
 * <p>The {@code householdId} is a separate, stable identifier shared by owners living in the same
 * household (same normalized last name and postcode): the first 12 upper-cased hex characters of the
 * SHA-256 of {@code '<lastName>|<postcode>'}. {@link AssignHouseholdId} assigns it and
 * {@link org.springframework.samples.petclinic.mapper.OwnerMapper} returns it. It no longer drives
 * duplicate detection — a soft match (see {@link AssignPossibleDuplicate}) instead triggers when the
 * {@code identityKey} differs but {@code soundex(lastName)} and postcode match.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The shared {@code householdId} derived from the last name and postcode: the first 12 upper-cased
     * hex characters of SHA-256 of {@code 'V2|<lastName>|<postcode>'} (mixing in the fixed
     * {@link MemberId#VERSION_TAG}), with the last name normalized
     * (lower-cased, runs of whitespace collapsed, trimmed) and a null/blank postcode contributing the
     * empty string. Because it is a pure function of {@code (lastName, postcode)}, two owners sharing
     * both deterministically receive the same value — they are, by definition, the same household.
     */
    public static String householdId(String lastName, String postcode) {
        String key = MemberId.VERSION_TAG + "|" + normalizeName(lastName) + "|"
                + (postcode == null ? "" : postcode.trim());
        return shaHex(key, 12);
    }

    /**
     * The derived {@code identityKey}: the full lower-case hex SHA-256 of
     * {@code 'V2' + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, with the
     * fixed {@link MemberId#VERSION_TAG} mixed in so no version-1 key recurs. A null telephone and a
     * blank/absent email each contribute the empty string.
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String tel = telephone == null ? "" : telephone;
        String em = (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
        String key = MemberId.VERSION_TAG + "|" + tel + "|" + em + "|" + soundex(lastName);
        return sha256Hex(key);
    }

    /**
     * American Soundex code of {@code value}: the retained first letter followed by three digits.
     * Adjacent letters (or letters separated only by {@code h}/{@code w}) sharing a code are coded once;
     * a vowel between them keeps both. Non-letters are ignored and the result is right-padded with
     * zeros to length four. A null/letterless value contributes the empty string.
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
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char prev = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // separators: leave prev unchanged so letters around them stay adjacent
            }
            char d = soundexCode(c);
            if (d != '0' && d != prev) {
                code.append(d);
            }
            prev = (d == '0') ? '0' : d; // a vowel (code 0) resets, so same codes across it are coded twice
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** Soundex digit for a letter; {@code '0'} for vowels and other uncoded letters. */
    private static char soundexCode(char c) {
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

    /** Lower-cases and collapses runs of whitespace to a single space, trimmed. Used for last name. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** First {@code n} upper-case hex characters of SHA-256 of {@code value}. */
    private static String shaHex(String value, int n) {
        return sha256(value, "%02X").substring(0, n);
    }

    /** Full lower-case hex SHA-256 of {@code value}. */
    private static String sha256Hex(String value) {
        return sha256(value, "%02x");
    }

    private static String sha256(String value, String byteFormat) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format(byteFormat, b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
