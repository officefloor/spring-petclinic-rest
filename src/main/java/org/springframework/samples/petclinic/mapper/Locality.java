package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's canonical region ('locality') from its {@code customerCode}. Owner identity is
 * now {@code <REGION>-<HASH8>} (assigned at registration from the owner's postcode and a hash of its
 * telephone and last name), so the locality is simply the REGION component — the part before the
 * first {@code '-'}. It is {@code "UNKNOWN"} when no code has been assigned.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s
 * {@code locality} expression) rather than a mapper {@code default} method: a
 * {@code String}-to-{@code String} method on the mapper interface would be
 * picked up by MapStruct as an automatic conversion for every String property.
 */
final class Locality {

    static final String UNKNOWN = "UNKNOWN";

    private Locality() {
    }

    /**
     * The REGION component of the owner's {@code customerCode} — the text before the first
     * {@code '-'} — or {@code "UNKNOWN"} when the code is absent or carries no region prefix.
     */
    static String of(String customerCode) {
        if (customerCode == null) {
            return UNKNOWN;
        }
        int dash = customerCode.indexOf('-');
        if (dash <= 0) {
            return UNKNOWN;
        }
        return customerCode.substring(0, dash);
    }
}
