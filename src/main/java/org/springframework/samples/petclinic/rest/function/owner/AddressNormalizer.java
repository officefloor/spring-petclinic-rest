package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Converts a raw street address into canonical form: leading and trailing whitespace
 * trimmed, internal runs of whitespace collapsed to a single space, upper-cased, and
 * common abbreviations expanded to their full word ({@code ST}&#8594;{@code STREET},
 * {@code RD}&#8594;{@code ROAD}, {@code AVE}&#8594;{@code AVENUE}). A {@code null} or
 * whitespace-only address normalizes to the empty string.
 *
 * <p>The single, shared definition of a normalized address: {@link NormalizeOwnerAddress}
 * uses it to store the canonical value, {@link ValidateOwnerFields} uses it to reject an
 * address that is blank once normalized, and {@link Household} uses it to compare
 * addresses for duplicate detection and the shared household identifier. The
 * transformation is idempotent, so normalizing an already-normalized address is a no-op.
 */
final class AddressNormalizer {

    /** Whole-token abbreviations expanded to their full word (keys already upper-cased). */
    private static final Map<String, String> ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /** Normalize {@code address} to canonical form, or the empty string when blank. */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
}
