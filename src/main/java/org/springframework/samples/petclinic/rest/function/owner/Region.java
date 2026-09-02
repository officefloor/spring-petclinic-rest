package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's plain region code (NSW, VIC, QLD or UNKNOWN) from the postcode (2xxx-&gt;NSW,
 * 3xxx-&gt;VIC, 4xxx-&gt;QLD), then by city (Sydney/Melbourne/Brisbane), else UNKNOWN. This is the
 * user-facing region behind {@code locality}, {@code timezone} and the owner segment; it never
 * carries the version tag, unlike the region code mixed into the identifiers.
 */
public final class Region {

    private Region() {
    }

    public static String of(Owner owner) {
        String pc = owner.getPostcode();
        if (pc != null && pc.matches("20\\d{2}")) {
            return "NSW";
        }
        if (pc != null && pc.matches("30\\d{2}")) {
            return "VIC";
        }
        if (pc != null && pc.matches("40\\d{2}")) {
            return "QLD";
        }
        return switch (String.valueOf(owner.getCity())) {
            case "Sydney" -> "NSW";
            case "Melbourne" -> "VIC";
            case "Brisbane" -> "QLD";
            default -> "UNKNOWN";
        };
    }
}
