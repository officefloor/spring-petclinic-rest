package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * The single definition of an owner's <em>member id</em>: the unified identifier
 * {@code <REGION><FY><HASH8><CHK>} that replaces the former customer code and membership
 * number. Its segments are:
 * <ul>
 *   <li>{@code REGION} — the region derived from the owner's postcode (see {@link Locality});</li>
 *   <li>{@code FY} — the two-digit {@link FiscalYear fiscal year} of the registration date;</li>
 *   <li>{@code HASH8} — the first 8 upper-case hex characters of SHA-256 over the
 *       version-2-tagged preimage of the region, normalized telephone and last name (see
 *       {@link IdentityVersion#tag(String)});</li>
 *   <li>{@code CHK} — a single {@link Luhn} check digit over the digits of
 *       {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>The region also feeds the {@code HASH8} preimage, so it is the region <em>used inside</em>
 * the identifier; the visible {@code REGION} prefix, however, stays the plain region code so the
 * derived 'locality', 'timezone' and owner segment read back the plain region, never the tag.
 *
 * <p>The region and fiscal-year segments can be read back from a formatted id (see
 * {@link #region} and {@link #fiscalYear}), so the derived region, locality and fiscal-year
 * fields all reference the member id rather than recomputing from the raw inputs.
 *
 * <p>Formatting and parsing only; the region and identity fields are supplied by the caller
 * ({@link AssignMemberId} derives the region and reads the stored fields).
 */
public final class MemberId {

    private static final int HASH_LENGTH = 8;

    /** Length of the two-digit fiscal-year segment that follows the region. */
    private static final int FY_LENGTH = 2;

    private MemberId() {
    }

    /**
     * Format a member id {@code <REGION><FY><HASH8><CHK>}, e.g.
     * {@code format("NSW", 2026-07-01, "+61412345678", "Smithers")} yields
     * {@code "NSW261A2B3C4D<chk>"}: region {@code NSW}, fiscal year {@code 26}, the 8 upper-case
     * hex HASH8 of SHA-256 over the version-2-tagged region, telephone and last name, and the
     * Luhn check digit over the digits of {@code <REGION><FY><HASH8>}.
     */
    public static String format(String region, LocalDate registrationDate, String telephone,
            String lastName) {
        String base = region + fySegment(registrationDate) + hash8(region, telephone, lastName);
        return base + Luhn.checkDigit(base);
    }

    /**
     * De-duplicate {@code memberId} against ids already in use. If {@code memberId} is free it is
     * returned unchanged; otherwise {@code -<n>} is appended with the smallest {@code n >= 2}
     * that yields an unused id. The suffix leaves {@link #region(String)} and
     * {@link #fiscalYear(String)} unchanged.
     *
     * @param memberId the freshly formatted id
     * @param inUse    tests whether a candidate id is already taken
     */
    public static String deduplicate(String memberId, Predicate<String> inUse) {
        if (!inUse.test(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + "-" + n;
            if (!inUse.test(candidate)) {
                return candidate;
            }
        }
    }

    /** The region segment of {@code memberId} (the leading letters before the fiscal-year
     *  digits), or {@code null} when the id is absent. */
    public static String region(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && !Character.isDigit(memberId.charAt(i))) {
            i++;
        }
        return memberId.substring(0, i);
    }

    /** The fiscal-year label {@code "FY<YY>"} read back from the two digits after the region of
     *  {@code memberId}, or {@code null} when the id is absent or malformed. */
    public static String fiscalYear(String memberId) {
        if (memberId == null) {
            return null;
        }
        int start = region(memberId).length();
        if (memberId.length() < start + FY_LENGTH) {
            return null;
        }
        return "FY" + memberId.substring(start, start + FY_LENGTH);
    }

    /** The two-digit fiscal-year segment for {@code registrationDate}. */
    private static String fySegment(LocalDate registrationDate) {
        return String.format("%02d", FiscalYear.shortYear(registrationDate));
    }

    /** The first 8 upper-case hex characters of SHA-256 over the version-2-tagged region,
     *  telephone and last name. */
    private static String hash8(String region, String telephone, String lastName) {
        return Sha256.hex(IdentityVersion.tag(region + "|" + telephone + "|" + lastName), HASH_LENGTH);
    }
}
