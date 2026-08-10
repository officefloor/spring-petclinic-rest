package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.IdentityVersion;

/**
 * Reads sub-values back out of an owner's unified {@code memberId}
 * ({@code <REGION><FY><HASH8><CHK>}), so the DTO fields derived from the identity stay consistent
 * with the id itself rather than being recomputed from other owner fields.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s {@code fiscalYear}
 * expression) rather than a mapper {@code default} method to avoid MapStruct picking it up as an
 * automatic conversion.
 */
final class MemberId {

    private MemberId() {
    }

    /**
     * The fiscal-year label {@code FY<YY>} taken from the two FY digits that follow the plain region
     * prefix of {@code memberId} (after the {@code "V2"} version tag), or {@code null} when the id is
     * absent or too short to carry them.
     */
    static String fiscalYear(String memberId) {
        if (memberId == null) {
            return null;
        }
        String region = Locality.of(memberId);
        String body = IdentityVersion.stripTag(memberId);
        if (body.length() < region.length() + 2) {
            return null;
        }
        return "FY" + body.substring(region.length(), region.length() + 2);
    }
}
