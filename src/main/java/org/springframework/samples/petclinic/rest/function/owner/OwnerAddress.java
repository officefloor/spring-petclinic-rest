package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for the owner-address normalization rule, applied whenever an owner is
 * created. A raw address is reduced to a canonical stored form: surrounding and repeated whitespace
 * collapsed to single spaces, upper-cased, and common street-type abbreviations expanded to their
 * full words ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
 *
 * <p>The same normalized form is what gets stored and returned, what the required-field check tests
 * for blankness, and what every address comparison uses — household duplicate detection and the
 * shared household id both reduce through {@link #normalize} (see {@link OwnerHousehold}).
 */
final class OwnerAddress {

    private OwnerAddress() {
    }

    /**
     * Reduces an address to its canonical form: trimmed, inner runs of whitespace collapsed to a
     * single space, upper-cased, with common abbreviations expanded token by token. A {@code null}
     * value yields an empty string.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "ST":
                    tokens[i] = "STREET";
                    break;
                case "RD":
                    tokens[i] = "ROAD";
                    break;
                case "AVE":
                    tokens[i] = "AVENUE";
                    break;
                default:
                    break;
            }
        }
        return String.join(" ", tokens);
    }

    /**
     * Composes the stored/returned flat address from the structured lines: the normalized
     * {@code addressLine1} with a single space and the normalized {@code addressLine2} appended when
     * an {@code addressLine2} is present (non-blank after normalization). Both lines are reduced
     * through {@link #normalize}, so the composed value is in the same canonical form.
     */
    static String compose(String addressLine1, String addressLine2) {
        String line1 = normalize(addressLine1);
        String line2 = normalize(addressLine2);
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }
}
