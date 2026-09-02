package org.springframework.samples.petclinic.service;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code timezone} is the IANA name mapped from its
 * {@code locality} region via a fixed region-to-timezone table (NSW->Australia/Sydney,
 * VIC->Australia/Melbourne, QLD->Australia/Brisbane), or {@code null} when the region is
 * not in the table. Kept as a small, self-contained unit alongside
 * {@link OwnerLocalityPolicy} so the rule can be applied from the read flow without adding
 * complexity to the mapper, controller, or service.
 */
public final class OwnerTimezonePolicy {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
        "NSW", "Australia/Sydney",
        "VIC", "Australia/Melbourne",
        "QLD", "Australia/Brisbane");

    private OwnerTimezonePolicy() {
    }

    /**
     * Derive the IANA {@code timezone} for the given owner from its locality region.
     *
     * @param owner the owner whose timezone to derive
     * @return the IANA timezone name, or {@code null} when the region has no mapping
     */
    public static String timezone(Owner owner) {
        return REGION_TIMEZONE.get(OwnerLocalityPolicy.locality(owner));
    }
}
