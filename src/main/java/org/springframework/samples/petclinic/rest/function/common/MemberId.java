package org.springframework.samples.petclinic.rest.function.common;

/**
 * Shared knowledge of the owner {@code memberId} string format, kept in one place so every
 * value derived from it stays consistent. The memberId is {@code <REGION><FY><HASH8><CHK>}
 * (no separators), where:
 *
 * <ul>
 *   <li>{@code REGION} is the region code derived from the owner's postcode (see
 *       {@link Localities#locality(String, String)});</li>
 *   <li>{@code FY} is the two-digit fiscal year of the (business-day-adjusted) registration
 *       date (see {@link FiscalYear#twoDigit(java.time.LocalDate)});</li>
 *   <li>{@code HASH8} is the first 8 upper-case hex characters of the SHA-256 digest over the
 *       normalized (E.164) telephone concatenated with the last name (see
 *       {@link Sha256#hexPrefix(String, int)}) — the same hash the region-and-hash identity
 *       carried;</li>
 *   <li>{@code CHK} is a single Luhn check digit (see {@link Luhn#checkDigit(String)}) computed
 *       over the digits of {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>It is a stable identity carrying no sequence number; when the {@link #base base} value
 * collides with an existing owner's memberId the producer appends {@code -<n>} (see
 * {@code org.springframework.samples.petclinic.rest.function.owner.AssignOwnerMemberId}).
 * Every value that follows from the memberId — the {@link #fiscalYearLabel fiscal-year label},
 * the create audit line and the {@link Localities#localityOf locality/timezone} read back from
 * its {@link #regionOf region} prefix — is built through this class so they all agree on the one
 * format.
 */
public final class MemberId {

    private MemberId() {
    }

    /**
     * The base {@code <REGION><FY><HASH8><CHK>} memberId for a region, fiscal year and the
     * owner's identity inputs, before any collision suffix. {@code lastName} is treated as
     * empty when null. {@code CHK} is the Luhn check digit over the digits of the
     * {@code <REGION><FY><HASH8>} core.
     */
    public static String base(String region, String twoDigitFiscalYear, String normalizedTelephone,
            String lastName) {
        String name = lastName == null ? "" : lastName;
        // Version 2: mix the fixed 'V2' tag and the region code into the hash input, so every
        // identifier changes and no value produced under version 1 recurs. The visible REGION
        // prefix stays the plain region, so the locality/timezone read back from it carry no tag.
        String hash8 = Sha256.hexPrefix(
                IdentityVersion.TAG + "|" + region + "|" + normalizedTelephone + name, 8);
        String core = region + twoDigitFiscalYear + hash8;
        return core + Luhn.checkDigit(core);
    }

    /**
     * The region encoded in a stored memberId — the leading run of alphabetic {@code REGION}
     * characters before the two-digit fiscal year — or {@code null} when the memberId is absent
     * or carries no region prefix (e.g. legacy records).
     */
    public static String regionOf(String memberId) {
        if (memberId == null) {
            return null;
        }
        int i = 0;
        while (i < memberId.length() && Character.isLetter(memberId.charAt(i))) {
            i++;
        }
        return i > 0 ? memberId.substring(0, i) : null;
    }

    /**
     * The fiscal-year label {@code FY<YY>} carried by a stored memberId — the two digits
     * following the {@link #regionOf region} prefix — or {@code null} when the memberId is
     * absent or carries no parseable fiscal-year segment (e.g. legacy records).
     */
    public static String fiscalYearLabel(String memberId) {
        String region = regionOf(memberId);
        if (region == null) {
            return null;
        }
        int start = region.length();
        if (memberId.length() < start + 2) {
            return null;
        }
        String fy = memberId.substring(start, start + 2);
        return fy.matches("[0-9]{2}") ? "FY" + fy : null;
    }
}
