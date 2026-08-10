package org.springframework.samples.petclinic.mapper;

import java.util.List;

/**
 * Derives an owner's canonical region ('locality') from its {@code memberId}. Owner identity is
 * {@code <REGION><FY><HASH8><CHK>} (assigned at registration from the owner's postcode, registration
 * date and a hash of its telephone and last name), so the locality is the leading REGION component.
 * It is {@code "UNKNOWN"} when no id has been assigned or the id carries no known region prefix.
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
     * The REGION component of the owner's {@code memberId} — its leading region prefix — or
     * {@code "UNKNOWN"} when the id is absent or begins with no known region.
     */
    static String of(String memberId) {
        if (memberId == null) {
            return UNKNOWN;
        }
        for (String region : REGIONS) {
            if (memberId.startsWith(region)) {
                return region;
            }
        }
        return UNKNOWN;
    }
}
