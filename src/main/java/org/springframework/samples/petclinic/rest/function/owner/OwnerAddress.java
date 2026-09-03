package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Normalizes an owner {@code address} to a canonical form. Surrounding and repeated internal
 * whitespace is trimmed and collapsed to a single space, the value is upper-cased, and common
 * street-type abbreviations are expanded as whole words: {@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}. So {@code '  12  main  st '} becomes {@code '12 MAIN STREET'} and
 * {@code '7 elm ave'} becomes {@code '7 ELM AVENUE'}. A null or whitespace-only value normalizes to
 * the empty string. The normalized value is what is stored, returned, and compared — for the
 * required-field check, household duplicate detection and the shared household id.
 */
final class OwnerAddress {

    /** Whole-word street-type abbreviations expanded to their full form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * @return the address trimmed, whitespace-collapsed, upper-cased and with abbreviations
     *         expanded, or the empty string when the value is null or blank.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase();
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
     * The composed {@code address} an owner stores and returns, preferring the structured fields when
     * present. When {@code addressLine1} is non-blank the result is the normalized {@code addressLine1}
     * with a single space and the normalized {@code addressLine2} appended when {@code addressLine2} is
     * present; otherwise it falls back to the flat {@code address}. All three inputs are normalized here,
     * so callers may pass the raw request values.
     *
     * @return the composed, normalized address, or the empty string when no address is supplied in
     *         either form.
     */
    static String compose(String addressLine1, String addressLine2, String flatAddress) {
        String line1 = normalize(addressLine1);
        if (line1.isEmpty()) {
            return normalize(flatAddress);
        }
        String line2 = normalize(addressLine2);
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }
}
