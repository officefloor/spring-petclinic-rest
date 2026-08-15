package org.springframework.samples.petclinic.rest.function.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds the owner's {@code memberId}, the single region-and-hash identity every other derived value
 * hangs off. The id is {@code '<REGION><FY><HASH8><CHK>'} where {@code REGION} is the region derived
 * from the postcode (falling back to the city, see {@link Localities}), {@code FY} is the two-digit
 * fiscal year (starting 1 July) of the business-day-adjusted {@code registrationDate}, {@code HASH8}
 * is the first eight upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)}, and
 * {@code CHK} is a single Luhn check digit computed over the digits of {@code <REGION><FY><HASH8>}
 * (e.g. {@code 'NSW261A2B3C4D7'}).
 *
 * <p>The telephone is already normalized to E.164 by the time an owner is built, so the hash is stable
 * for a given owner regardless of how the telephone was typed. The {@code FY} segment matches the
 * owner's {@code fiscalYear} label (see {@link FiscalYears#labelOf(Owner)}).
 */
public final class MemberIds {

    private MemberIds() {
    }

    /** The {@code '<REGION><FY><HASH8><CHK>'} member id for {@code owner}. */
    public static String of(Owner owner) {
        String base = identityRegionOf(owner)
                + fiscalYearSegment(owner.getRegistrationDate())
                + hash8(owner.getTelephone(), owner.getLastName());
        return base + CheckDigits.luhnOf(base);
    }

    /**
     * The version-2 region code embedded in the {@code memberId}: the plain region (see
     * {@link #localityOf(Owner)}) with the fixed {@link IdentityVersion#TAG version tag} appended, so
     * the id can never equal one produced under version 1. This is the region code used <em>inside</em>
     * the identifier only; the user-facing {@code locality} keeps the plain region.
     */
    private static String identityRegionOf(Owner owner) {
        return Localities.of(owner.getPostcode(), owner.getCity()) + IdentityVersion.TAG;
    }

    /**
     * De-duplicates {@code memberId} against {@code taken}: if no existing owner already uses it the
     * id is returned unchanged, otherwise {@code '-<n>'} is appended with the smallest {@code n >= 2}
     * that makes the result unique (e.g. {@code 'NSW261A2B3C4D7-2'}).
     */
    public static String deduplicate(String memberId, java.util.Collection<String> taken) {
        if (!taken.contains(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The first eight upper-case hex characters of {@code SHA-256(telephone + lastName)}, over the
     * UTF-8 bytes of the concatenation. A {@code null} component contributes nothing.
     */
    public static String hash8(String telephone, String lastName) {
        String input = safe(telephone) + safe(lastName);
        byte[] digest = sha256(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }

    /** The owner's region: derived from the postcode, falling back to the city (see {@link Localities}). */
    public static String localityOf(Owner owner) {
        return Localities.of(owner.getPostcode(), owner.getCity());
    }

    /** The two-digit fiscal year (starting 1 July) of {@code date}, zero-padded. */
    private static String fiscalYearSegment(LocalDate date) {
        return String.format("%02d", Math.floorMod(FiscalYears.yearOf(date), 100));
    }

    private static byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
