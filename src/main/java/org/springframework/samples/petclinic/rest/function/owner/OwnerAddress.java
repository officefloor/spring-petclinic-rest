package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * The single definition of how an owner's {@code address} is normalized. Applied whenever an owner is
 * created (see {@link ValidateOwner}, which stores and returns the normalized value), so the stored,
 * abbreviation-expanded address is consistent.
 *
 * <p>Normalization: strip surrounding whitespace and collapse internal runs to a single space,
 * upper-case, then expand common abbreviations token-by-token ({@code ST}→{@code STREET},
 * {@code RD}→{@code ROAD}, {@code AVE}→{@code AVENUE}). A value that is blank after this is normalized
 * to the empty string, which {@link ValidateOwner} treats as a missing required field.
 *
 * <p>This is also where the structured address lines are composed into the single stored
 * {@code address} (see {@link #compose(String, String)}), so every rule about how an address is
 * shaped — normalization and composition alike — lives here, once.
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

    /**
     * Compose the stored {@code address} from the structured address lines: the normalized
     * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended when
     * {@code addressLine2} is present (non-blank once normalized). Both lines are normalized here (see
     * {@link #normalize(String)}), so callers may pass raw or already-normalized values. Returns the
     * empty string when {@code addressLine1} normalizes to empty.
     */
    public static String compose(String addressLine1, String addressLine2) {
        String line1 = normalize(addressLine1);
        if (line1.isEmpty()) {
            return "";
        }
        String line2 = normalize(addressLine2);
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }
}
