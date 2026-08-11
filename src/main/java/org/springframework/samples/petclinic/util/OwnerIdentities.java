package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone;

/**
 * Derives an owner's {@code identityKey}: the single value all create-time duplicate detection is
 * expressed through. Under the version-2 algorithm the key is the lower-case hex SHA-256 of
 * {@code "V2" + "|" + normalizedTelephone + "|" + lowerEmail + "|" + soundex(lastName)}, so two owners are
 * duplicates only when all three normalized components agree. Because the telephone is part of the
 * key, two owners sharing a last name (same soundex) and postcode but with <em>different</em>
 * telephones have different keys and are both allowed — they are a soft match, not a hard duplicate.
 *
 * <p>Each component is normalized the same way for every owner so the comparison is stable:
 * the telephone to E.164 (see {@link OwnerTelephone}), the email trimmed and lower-cased (blank or
 * absent represented as the empty string), and the last name reduced to its Soundex code. Every
 * normalization is idempotent, so the returned key equals the one used for comparison.
 */
public final class OwnerIdentities {

    /**
     * The fixed version tag mixed into every derived identifier by the version-2 algorithm. It is
     * folded into the {@link #memberId(Owner) memberId} (via its region segment), the
     * {@link #householdId(Owner) householdId} and the {@link #identityKey(Owner) identityKey}, so
     * every identifier changes and no value produced under version 1 is produced again. The tag
     * appears <em>only</em> inside the identifiers — never in the user-facing {@code locality},
     * {@code timezone} or the owner segment's derived region, which stay the plain region code.
     */
    private static final String VERSION_TAG = "V2";

    private OwnerIdentities() {
    }

    public static String identityKey(Owner owner) {
        return sha256Hex(VERSION_TAG + "|"
                + normalizeTelephone(owner.getTelephone()) + "|"
                + normalizeEmail(owner.getEmail()) + "|"
                + soundex(owner.getLastName()));
    }

    /**
     * The American Soundex code of {@code name}: its first letter (upper-cased) followed by three
     * digits derived from the remaining consonants, zero-padded or truncated to length four. Letters
     * are coded b/f/p/v→1, c/g/j/k/q/s/x/z→2, d/t→3, l→4, m/n→5, r→6; vowels (a/e/i/o/u/y) reset the
     * run so an equal digit on either side is counted twice, while h/w are transparent and never
     * break a run. Non-letters are ignored; a name with no letters yields the empty string.
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
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char prev = soundexDigit(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // transparent: neither coded nor a break in the run
            }
            char d = soundexDigit(c);
            if (d != '0') {
                if (d != prev) {
                    code.append(d);
                }
                prev = d;
            }
            else {
                prev = '0'; // a vowel resets the run
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
     * The owner's <em>current primary identifier</em>: the single value that owner-scoped business
     * events publish to identify the owner. This is the unified {@link Owner#getMemberId() memberId},
     * so every event carries the memberId without any change at the call sites.
     */
    public static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Builds the owner's unified {@code memberId}: {@code '<REGION><FY><HASH8><CHK>'} (no separators).
     * REGION is the version-2 identity region — the canonical region derived from the owner's postcode
     * (falling back to city — see {@link Localities}) with the fixed {@code 'V2'} version tag mixed in
     * (see {@link #identityRegion(Owner)}); FY is the two-digit fiscal year of the owner's {@code registrationDate}
     * (fiscal years start 1 July — see {@link FiscalYears}); HASH8 is the first 8 upper-case hex
     * characters of SHA-256 over {@code normalizedTelephone + lastName}; and CHK is a single Luhn
     * check digit computed over the decimal digits of {@code '<REGION><FY><HASH8>'} (see
     * {@link CheckDigits}). The telephone is normalized to E.164 the same way as in
     * {@link #identityKey(Owner)}, so owners differing only in telephone formatting share the HASH8.
     */
    public static String memberId(Owner owner) {
        String region = identityRegion(owner);
        String fy = FiscalYears.yearSegment(owner.getRegistrationDate());
        String base = region + fy + hash8(owner);
        return base + CheckDigits.luhn(base);
    }

    /**
     * The region code used <em>inside</em> the identifiers: the canonical region (derived from
     * postcode, falling back to city — see {@link Localities}) with the fixed {@link #VERSION_TAG}
     * mixed in. This is what the version-2 {@link #memberId(Owner) memberId} embeds, so its region
     * segment differs from every version-1 value. The plain region without the tag remains the
     * user-facing {@code locality} and the owner segment's region.
     */
    private static String identityRegion(Owner owner) {
        return Localities.localityFor(owner.getCity(), owner.getPostcode()) + VERSION_TAG;
    }

    /**
     * The first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName} —
     * the HASH8 segment shared by the {@link #memberId(Owner) memberId} and the region-and-hash
     * identity.
     */
    private static String hash8(Owner owner) {
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        return sha256Hex(normalizeTelephone(owner.getTelephone()) + lastName)
                .substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /**
     * The owner's deterministic {@code householdId}: the first 12 hex characters of SHA-256 over
     * {@code "V2" + "|" + normalizedLastName + "|" + postcode}. Owners with the same last name (normalized to a
     * trimmed, whitespace-collapsed, lower-cased form) and the same postcode therefore compute the
     * same identifier and belong to the same household automatically — no request has to declare it.
     */
    public static String householdId(Owner owner) {
        String lastName = normalizeName(owner.getLastName());
        String postcode = owner.getPostcode() == null ? "" : owner.getPostcode().trim();
        return sha256Hex(VERSION_TAG + "|" + lastName + "|" + postcode).substring(0, 12);
    }

    /** Full lower-case hex SHA-256 over the UTF-8 bytes of {@code value}. */
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
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }

    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private static String normalizeTelephone(String telephone) {
        try {
            return OwnerTelephone.toE164(telephone);
        }
        catch (InvalidTelephoneException ex) {
            // A value that cannot form a valid E.164 number cannot collide with one that can;
            // represent it by its raw form (or empty when absent) so comparison stays total.
            return telephone == null ? "" : telephone;
        }
    }

    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? "" : trimmed.toLowerCase(Locale.ROOT);
    }
}
