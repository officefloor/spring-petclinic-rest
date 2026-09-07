package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner's member id — the single unified identity every other value hangs off. It is
 * {@code '<REGION><FY><HASH8><CHK>'}:
 * <ul>
 * <li>REGION is the canonical region derived from the owner's postcode (falling back to the city
 *     table via {@link Locality});</li>
 * <li>FY is the two-digit fiscal year (starting 1 July) of the business-day-adjusted
 *     registration date (see {@link FiscalYear});</li>
 * <li>HASH8 is the first 8 UPPER-case hex characters of SHA-256 over the normalized (E.164)
 *     telephone concatenated with the last name — the same hash used by the region-and-hash
 *     identity;</li>
 * <li>CHK is a single Luhn check digit ({@link CheckDigit}) computed over the digits of
 *     {@code <REGION><FY><HASH8>}.</li>
 * </ul>
 *
 * <p>There are no sequence numbers: the id is a pure function of region, fiscal year, telephone
 * and last name, so it needs no repository scan and is stable for a given owner. The create audit
 * line and the owner segment are built from this one value.
 */
public final class MemberId {

    private MemberId() {
    }

    /** The {@code '<REGION><FY><HASH8><CHK>'} member id for {@code owner}. */
    public static String of(Owner owner) {
        // REGION is the version-2 region code: the plain region with the fixed 'V2' tag mixed in,
        // so this identifier changes and never reproduces a version-1 value. The user-facing
        // locality keeps the plain region.
        String region = IdentityVersion.region(Locality.of(owner.getCity(), owner.getPostcode()));
        String fy = String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
        String hash8 = ShaHex.upperPrefix(owner.getTelephone() + owner.getLastName(), 8);
        String base = region + fy + hash8;
        return base + CheckDigit.of(base);
    }

    /**
     * De-duplicate a computed member id against those already in use. When {@code memberId}
     * collides with an existing owner's member id, the smallest {@code n} of 2 or more is
     * appended as {@code '-<n>'} to make it unique; a member id that does not collide is returned
     * unchanged (as is a {@code null} member id).
     *
     * @param memberId       the computed member id to make unique
     * @param existingIds    the member ids already assigned to other owners
     * @return the de-duplicated member id
     */
    public static String dedupe(String memberId, java.util.Set<String> existingIds) {
        if (memberId == null || !existingIds.contains(memberId)) {
            return memberId;
        }
        for (int n = 2; ; n++) {
            String candidate = memberId + "-" + n;
            if (!existingIds.contains(candidate)) {
                return candidate;
            }
        }
    }
}
