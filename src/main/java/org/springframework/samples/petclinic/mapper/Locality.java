package org.springframework.samples.petclinic.mapper;

import java.util.List;

import org.springframework.samples.petclinic.model.IdentityVersion;

/**
 * Derives an owner's canonical region ('locality') from its {@code memberId}. Owner identity is
 * {@code <REGION><FY><HASH8><CHK>} where REGION is the {@code "V2"} version tag followed by the plain
 * region derived from the postcode. The user-facing locality is that <em>plain</em> region — the
 * version tag is an internal identifier detail and never appears in the locality — so the tag is
 * stripped before matching. It is {@code "UNKNOWN"} when no id has been assigned or the id carries no
 * known region.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s
 * {@code locality} expression) rather than a mapper {@code default} method: a
 * {@code String}-to-{@code String} method on the mapper interface would be
 * picked up by MapStruct as an automatic conversion for every String property.
 */
final class Locality {

    static final String UNKNOWN = "UNKNOWN";

    /** The known region prefixes a memberId can start with (everything else is {@code UNKNOWN}). */
    private static final List<String> REGIONS = List.of("NSW", "VIC", "QLD");

    private Locality() {
    }

    /**
     * The plain REGION component of the owner's {@code memberId} — its leading region prefix once the
     * {@code "V2"} version tag is stripped — or {@code "UNKNOWN"} when the id is absent or begins with
     * no known region.
     */
    static String of(String memberId) {
        if (memberId == null) {
            return UNKNOWN;
        }
        String body = IdentityVersion.stripTag(memberId);
        for (String region : REGIONS) {
            if (body.startsWith(region)) {
                return region;
            }
        }
        return UNKNOWN;
    }
}
