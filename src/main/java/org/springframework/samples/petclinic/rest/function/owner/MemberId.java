package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * The owner's {@code memberId} string format, kept in one place: how the id is assembled from its
 * parts, how its HASH8 segment is computed and how its REGION prefix is read back out. The id is
 * {@code <REGION><FY><HASH8><CHK>} (no separators) where REGION is the canonical region (see
 * {@link Locality}), FY is the two-digit fiscal year of the registration date (see
 * {@link FiscalYear}), HASH8 is the first 8 upper-case hex characters of the SHA-256 digest of
 * {@code normalizedTelephone + lastName}, and CHK is a single Luhn check digit computed over the
 * decimal digits of {@code <REGION><FY><HASH8>} (e.g. {@code NSW271A2B3C4D5}).
 *
 * <p>This unifies the former {@code customerCode}, {@code membershipNumber} and {@code checkDigit}
 * into one identifier: the fiscal-year segment that {@code membershipNumber} carried is now the FY
 * segment here, and the Luhn digit that {@code checkDigit} exposed is now the CHK segment here.
 *
 * <p>{@link AssignMemberId} builds the id through here and {@link Locality} reads its region prefix
 * through here, so neither has to know the id's internal layout.
 */
public final class MemberId {

    private MemberId() {
    }

    /**
     * The member id assembled from its REGION, FY and HASH8 segments plus a trailing Luhn CHK digit:
     * {@code <REGION><FY><HASH8><CHK>}. FY is the two low-order digits of {@code fiscalYear} and CHK
     * is the Luhn check digit over the decimal digits of {@code <REGION><FY><HASH8>}.
     */
    public static String of(String region, int fiscalYear, String hash8) {
        String body = String.format("%s%02d%s", region, Math.floorMod(fiscalYear, 100), hash8);
        return body + Luhn.of(body);
    }

    /**
     * The HASH8 segment: the first 8 upper-case hex characters of SHA-256 over
     * {@code (normalizedTelephone + lastName)}. The telephone is expected already normalized to
     * canonical E.164 form (see {@link OwnerIdentity#normalizedTelephone(String)}), the same
     * normalization the identity key uses, so the segment is derived purely from the owner's own
     * fields and is seed-independent.
     */
    public static String hash8(String normalizedTelephone, String lastName) {
        String input = normalizedTelephone + (lastName == null ? "" : lastName);
        return Sha256.hex(input).substring(0, 8).toUpperCase();
    }

    /**
     * The REGION prefix of a {@code <REGION><FY><HASH8><CHK>} member id — the leading segment that
     * matches one of {@code knownRegions} — or {@code null} when the id is absent or carries no such
     * prefix. The regions never prefix one another, so the match is unambiguous.
     */
    public static String region(String memberId, Set<String> knownRegions) {
        if (memberId == null) {
            return null;
        }
        for (String region : knownRegions) {
            if (memberId.startsWith(region)) {
                return region;
            }
        }
        return null;
    }
}
