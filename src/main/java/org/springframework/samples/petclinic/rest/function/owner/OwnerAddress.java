package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared address normalization for the owner create pipeline. An address is normalized by
 * trimming and collapsing internal runs of whitespace to a single space, upper-casing, and
 * expanding common street-type abbreviations token-by-token ({@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}). So {@code "  12  main  st "} becomes
 * {@code "12 MAIN STREET"} and {@code "7 elm ave"} becomes {@code "7 ELM AVENUE"}.
 *
 * <p>The normalized value is what {@code POST /api/owners} stores and returns as {@code address},
 * and it is the form every address comparison (household duplicate detection and the shared
 * household id) is done against. Normalization is idempotent: normalizing an already-normalized
 * address yields the same value.
 */
final class OwnerAddress {

    /** Whole-token abbreviations expanded to their full street type. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * Normalizes an address. A {@code null} input is left as {@code null}; an input that is empty
     * or only whitespace normalizes to {@code ""} (which the required-field check treats as blank).
     */
    static String normalize(String input) {
        if (input == null) {
            return null;
        }
        String collapsed = input.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }

    /** The normalized address, with {@code null} mapped to {@code ""} for safe comparison. */
    static String normalizeForCompare(String input) {
        String normalized = normalize(input);
        return normalized == null ? "" : normalized;
    }

    /**
     * Normalizes whichever address fields the request supplies and composes the effective flat
     * {@code address}. The structured fields are preferred: when {@code addressLine1} is present
     * (non-blank after normalization) the composed {@code address} is the normalized
     * {@code addressLine1}, with a single space and the normalized {@code addressLine2} appended
     * when {@code addressLine2} is present. Otherwise the flat {@code address} is used, normalized
     * the same way. The normalized structured fields are stored back on the request so every later
     * step persists and returns them; the composed {@code address} is what everything that reads the
     * owner's address then works against.
     */
    static void applyStructured(OwnerFieldsDto request) {
        String line1 = normalize(request.getAddressLine1());
        String line2 = normalize(request.getAddressLine2());
        request.setAddressLine1(line1);
        request.setAddressLine2(line2);
        if (line1 != null && !line1.isBlank()) {
            request.setAddress(compose(line1, line2));
        }
        else {
            request.setAddress(normalize(request.getAddress()));
        }
    }

    /**
     * Composes the flat address from the already-normalized structured lines: {@code line1}, with a
     * single space and {@code line2} appended when {@code line2} is present (non-blank).
     */
    static String compose(String line1, String line2) {
        if (line2 != null && !line2.isBlank()) {
            return line1 + " " + line2;
        }
        return line1;
    }
}
