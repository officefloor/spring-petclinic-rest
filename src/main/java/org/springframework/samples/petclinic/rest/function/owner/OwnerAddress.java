package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Shared normalization for the owner postal address. The canonical stored and returned form is
 * trimmed, has internal whitespace collapsed to single spaces, is upper-cased and has common
 * street-type abbreviations expanded ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). Applied when an owner is created so the stored address, the required-field
 * check and every household comparison (duplicate detection and the shared household id) all use the
 * same form.
 */
final class OwnerAddress {

    /** Whole-token street-type abbreviations expanded to their full form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * @return the normalized address (trimmed, whitespace-collapsed, upper-cased, abbreviations
     * expanded), or an empty string when {@code address} is {@code null} or blank after trimming.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    /**
     * @return the normalized value, or {@code null} when it is {@code null} or blank after
     * normalization. Used to store the structured address lines in their canonical form while
     * treating a blank line as absent.
     */
    static String normalizeOrNull(String value) {
        String normalized = normalize(value);
        return normalized.isEmpty() ? null : normalized;
    }

    /**
     * Composes the canonical, normalized address from the structured or flat inputs. When
     * {@code addressLine1} is non-blank the structured form wins: the composed address is the
     * normalized {@code addressLine1}, with a single space and the normalized {@code addressLine2}
     * appended when that line is present. Otherwise the flat {@code address} is normalized and used.
     *
     * @return the composed address, or an empty string when no address is supplied in either form.
     */
    static String compose(String addressLine1, String addressLine2, String address) {
        String line1 = normalizeOrNull(addressLine1);
        if (line1 == null) {
            return normalize(address);
        }
        String line2 = normalizeOrNull(addressLine2);
        return line2 == null ? line1 : line1 + " " + line2;
    }
}
