package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.util.Locality;

/**
 * Derives an owner's {@code identityKey} — the single value all duplicate detection is expressed
 * through. The key is the lower-case SHA-256 hex of {@code normalizedTelephone + '|' + lowerEmail +
 * '|' + soundex(lastName)}: a full-key match with an existing owner is a duplicate (409); any
 * differing component (a different telephone, email, or a last name with a different soundex) is a
 * distinct identity that is allowed.
 *
 * <p>Not an OfficeFloor function class — a plain helper shared by the create-owner steps so the key
 * derived at the uniqueness check, the soft-match check, and the key returned in the response are all
 * computed the same way.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Assembles the identity key: the lower-case SHA-256 hex over
     * {@code 'V2' + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, the fixed
     * version-2 tag re-salting the key so every value differs from its version-1 form while duplicate
     * detection is unaffected (the tag is constant across owners). {@code telephone} is
     * expected in E.164 form; {@code email} is normalized here (trimmed, lower-cased) and a null/blank
     * value contributes an empty component; the last name contributes its {@link #soundex soundex}
     * code.
     */
    public static String key(String telephone, String email, String lastName) {
        String tel = telephone == null ? "" : telephone;
        String mail = normalizeEmail(email);
        mail = mail == null ? "" : mail;
        String sdx = soundex(lastName);
        return sha256hex(Locality.IDENTITY_VERSION_TAG + "|" + tel + "|" + mail + "|" + sdx);
    }

    /**
     * American Soundex code for {@code name}: the retained first letter followed by three digits (zero
     * padded). Non-letters are ignored; consonants map to digits (b,f,p,v→1; c,g,j,k,q,s,x,z→2;
     * d,t→3; l→4; m,n→5; r→6) with adjacent equal codes collapsed, collapse preserved across
     * {@code h}/{@code w} and broken by a vowel. A null/blank name yields {@code "0000"}.
     */
    public static String soundex(String name) {
        String letters = name == null ? "" : name.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (letters.isEmpty()) {
            return "0000";
        }
        StringBuilder code = new StringBuilder();
        code.append(letters.charAt(0));
        char previous = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            // 'h' and 'w' are transparent: keep the previous code so equal codes on either side collapse.
            if (c == 'H' || c == 'W') {
                continue;
            }
            char digit = soundexCode(c);
            if (digit != '0' && digit != previous) {
                code.append(digit);
            }
            // A vowel breaks the run so equal codes either side of it are coded twice; a consonant sets
            // the run so an immediately repeated code collapses.
            previous = isVowel(c) ? '0' : digit;
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

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

    private static boolean isVowel(char c) {
        return c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U' || c == 'Y';
    }

    /** Lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    private static String sha256hex(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Best-effort E.164 for a stored telephone; falls back to the raw value when it cannot form E.164. */
    public static String toE164(String telephone) {
        try {
            return TelephoneE164.toE164(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone;
        }
    }

    /** Trimmed, lower-cased email, or null when absent — matching {@link NormalizeOwnerEmail}. */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Stable, shared household id: the first 12 upper-hex chars of SHA-256 over the fixed version-2 tag,
     * the canonical last name and the postcode ({@code 'V2' + '|' + normalizedLastName + '|' + postcode}),
     * so every owner with the same last name and postcode derives the same value automatically — the
     * household is keyed on (lastName, postcode) alone. The 'V2' tag is constant across owners, so it only
     * re-salts the value (no version-1 value is reproduced) without changing household grouping. A null
     * postcode contributes an empty component.
     */
    public static String householdId(String lastName, String postcode) {
        String normalizedLastName = canonicalName(lastName);
        String pc = postcode == null ? "" : postcode;
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest((Locality.IDENTITY_VERSION_TAG + "|" + normalizedLastName + "|" + pc)
                            .getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-cased, trimmed, with any run of whitespace collapsed to a single space. */
    private static String canonicalName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
