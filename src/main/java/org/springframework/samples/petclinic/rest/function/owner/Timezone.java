package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's IANA timezone from the region (see {@link Locality}) via the fixed
 * region-to-timezone table (NSW->Australia/Sydney, VIC->Australia/Melbourne,
 * QLD->Australia/Brisbane). Any region not listed yields {@code null}.
 */
public final class Timezone {

    private static final Map<String, String> REGION_TIMEZONE = Map.of(
            "NSW", "Australia/Sydney", "VIC", "Australia/Melbourne", "QLD", "Australia/Brisbane");

    private Timezone() {
    }

    public static String of(Owner owner) {
        return REGION_TIMEZONE.get(Locality.of(owner));
    }
}
