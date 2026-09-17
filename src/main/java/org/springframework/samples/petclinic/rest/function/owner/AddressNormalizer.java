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
 * uses it to store the canonical value and {@link ValidateOwnerFields} uses it to reject an
 * address that is blank once normalized. The transformation is idempotent, so normalizing an
 * already-normalized address is a no-op.
 *
 * <p>An address may arrive in two forms: the structured {@code addressLine1} /
 * {@code addressLine2} fields (preferred) or the flat {@code address} field (kept for
 * backward compatibility). {@link #line1} picks the structured first line when present,
 * falling back to the flat address, and {@link #compose} builds the single canonical address
 * string returned to clients.
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

    /**
     * The effective first address line: the structured {@code addressLine1} when non-blank,
     * otherwise the flat {@code address}. This is the single point that prefers the structured
     * form over the flat one.
     */
    static String line1(String addressLine1, String flatAddress) {
        return isBlank(addressLine1) ? flatAddress : addressLine1;
    }

    /**
     * The composed, canonical address: the normalized {@code addressLine1}, with a single space
     * and the normalized {@code addressLine2} appended when {@code addressLine2} is present.
     */
    static String compose(String addressLine1, String addressLine2) {
        String first = normalize(addressLine1);
        String second = normalize(addressLine2);
        return second.isEmpty() ? first : first + " " + second;
    }

    /** Whether {@code value} is null or contains only whitespace. */
    static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
