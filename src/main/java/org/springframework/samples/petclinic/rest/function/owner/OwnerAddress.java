package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * The single definition of how an owner's {@code address} is normalized. Applied whenever an owner is
 * created (see {@link ValidateOwner}, which stores and returns the normalized value) and used by every
 * comparison of addresses (see {@link OwnerHousehold}), so stored values and household matching stay
 * consistent.
 *
 * <p>Normalization: strip surrounding whitespace and collapse internal runs to a single space,
 * upper-case, then expand common abbreviations token-by-token ({@code ST}→{@code STREET},
 * {@code RD}→{@code ROAD}, {@code AVE}→{@code AVENUE}). A value that is blank after this is normalized
 * to the empty string, which {@link ValidateOwner} treats as a missing required field.
 */
public final class OwnerAddress {

    /** Whole-token abbreviation expansions, keyed by the upper-cased token. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder result = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return result.toString();
    }
}
