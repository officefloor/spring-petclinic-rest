package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey} — the single value all duplicate detection is expressed
 * through.
 *
 * <p>The key is the lower-case hex SHA-256 over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Two owners are duplicates
 * only when their keys are equal (see {@link CheckIdentityUnique}). Because the telephone is part of
 * the key, two owners with the same last name and postcode but different telephones have different
 * keys and are both allowed — the second is created as a soft match (see {@link PossibleDuplicate}).
 *
 * <p>The three components are normalized so the key is stable: the telephone to E.164 form (see
 * {@link TelephoneE164}), the email lower-cased and trimmed, and the last name reduced to its Soundex
 * code so equivalent-sounding surnames share it.
 */
public final class OwnerIdentity {

    /**
     * The identity-derivation version tag mixed into every version-2 identifier — the {@code memberId}
     * (its region segment and HASH8), the {@code householdId} and the {@code identityKey}. Mixing the
     * {@code "V2"} tag into each hashed pre-image changes every identifier and guarantees no value a
     * version-1 owner received is reproduced. It is confined to the identifiers: the user-facing
     * {@code locality}/{@code timezone} and the owner segment's derived region stay the plain region
     * code (e.g. {@code "NSW"}, never {@code "NSWV2"}).
     */
    public static final String VERSION_TAG = "V2";

    private OwnerIdentity() {
    }

    /** The identity key of a persisted owner, from its stored telephone, email and last name. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * The owner's current <em>primary identifier</em> — the single externally-visible handle an owner
     * is known by. That is the {@code memberId} (the unified {@code <REGION><FY><HASH8><CHK>} identity,
     * see {@link MemberId}), so everything expressed through the primary identifier (e.g. the
     * {@code OWNER_CREATED} structured event, see {@link OwnerCreatedEvent}) follows from it.
     */
    public static String primaryIdentifier(Owner owner) {
        return owner.getMemberId();
    }

    /**
     * Assemble the identity key: the lower-case hex SHA-256 over the fixed {@link #VERSION_TAG}, the
     * normalized telephone, lower-cased email and Soundex of the last name, joined by {@code '|'}. The
     * version tag makes this a version-2 key, distinct from any produced under version 1.
     */
    public static String key(String telephone, String email, String lastName) {
        return sha256HexFull(VERSION_TAG + "|" + normalizeTelephone(telephone) + "|"
                + normalizeEmail(email) + "|" + soundex(lastName));
    }

    /**
     * The stable, shared household identifier for an owner's last name and postcode: the first 12
     * upper-case hex characters of SHA-256 over {@code VERSION_TAG + '|' + normalizedLastName + '|' +
     * postcode} (the version-2 tag prefix, then the last name
     * lower-cased with whitespace collapsed; postcode trimmed). Owners with the same last name and
     * postcode get the same value automatically, regardless of creation order, so the household is
     * keyed on {@code (lastName, postcode)}. Returns {@code null} when there is no postcode — an owner
     * without a postcode is not in any household.
     */
    public static String householdIdFor(String lastName, String postcode) {
        String normalizedPostcode = normalizePostcode(postcode);
        if (normalizedPostcode == null) {
            return null;
        }
        return sha256Hex(VERSION_TAG + "|" + normalizeLastName(lastName) + "|" + normalizedPostcode, 12);
    }

    /**
     * The HASH8 segment of an owner's {@code memberId}: the first 8 upper-case hex characters of
     * SHA-256 over {@code VERSION_TAG + normalizedTelephone + lastName}. The telephone is normalized to E.164 form
     * (null/invalid becomes empty) so the hash is stable across equivalent phone formats; the last
     * name is taken as stored.
     */
    public static String memberIdHash(String telephone, String lastName) {
        return sha256Hex(VERSION_TAG + normalizeTelephone(telephone) + (lastName == null ? "" : lastName), 8);
    }

    /**
     * The American Soundex code of a last name: the retained first letter followed by up to three
     * digits, zero-padded to length 4 (e.g. {@code "Robert" -> "R163"}). Letters are coded
     * B,F,P,V=1; C,G,J,K,Q,S,X,Z=2; D,T=3; L=4; M,N=5; R=6; vowels and Y are ignored; adjacent letters
     * with the same code (and letters separated only by H or W) collapse to one digit. A null or
     * letter-less name yields the empty string, so such owners simply share the "no surname" bucket.
     */
    public static String soundex(String lastName) {
        if (lastName == null) {
            return "";
        }
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < lastName.length(); i++) {
            char c = lastName.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(Character.toUpperCase(c));
            }
        }
        if (letters.length() == 0) {
            return "";
        }
        StringBuilder code = new StringBuilder().append(letters.charAt(0));
        char lastCode = soundexCode(letters.charAt(0));
        for (int i = 1; i < letters.length() && code.length() < 4; i++) {
            char c = letters.charAt(i);
            if (c == 'H' || c == 'W') {
                continue; // H and W never separate two same-coded consonants
            }
            char digit = soundexCode(c);
            if (digit != '0' && digit != lastCode) {
                code.append(digit);
            }
            lastCode = digit; // vowels ('0') reset, so a following same-coded consonant is coded
        }
        while (code.length() < 4) {
            code.append('0');
        }
        return code.toString();
    }

    /** The Soundex digit for a letter, or {@code '0'} for vowels, Y and any uncoded letter. */
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

    /** Full lower-case hex SHA-256 over the UTF-8 bytes of {@code input} (64 characters). */
    private static String sha256HexFull(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** First {@code length} upper-case hex chars of SHA-256 over the UTF-8 bytes of {@code input}. */
    private static String sha256Hex(String input, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, length).toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Telephone in E.164 form for comparison; null/invalid becomes empty. */
    private static String normalizeTelephone(String telephone) {
        String e164 = TelephoneE164.normalize(telephone);
        return e164 == null ? "" : e164;
    }

    /** Email lower-cased and trimmed so comparison ignores case; null/blank becomes empty. */
    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** Last name lower-cased with runs of whitespace collapsed, so comparison ignores case/spacing. */
    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Postcode trimmed; null or blank becomes null (no household). */
    private static String normalizePostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        String trimmed = postcode.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
