package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Canonicalises a free-text street address so it is stored, returned and compared in one consistent
 * form. Whitespace is trimmed and any run collapsed to a single space, the text is upper-cased, and
 * common abbreviations are expanded per token: {@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}. So {@code '  12  main  st '} becomes {@code '12 MAIN STREET'}. The result is
 * idempotent, so re-normalising an already-normalised address leaves it unchanged.
 *
 * <p>A {@code null}, empty or whitespace-only address normalises to the empty string, which the
 * required-field check treats as blank.
 *
 * <p>Not an OfficeFloor function class — a plain helper shared by the create-owner steps.
 */
public final class AddressNormalizer {

    private AddressNormalizer() {
    }

    /**
     * The effective, normalized street address for a request. The structured form is preferred: when
     * {@code addressLine1} is non-blank the result is its normalized value, with a single space and the
     * normalized {@code addressLine2} appended when {@code addressLine2} is present. Otherwise it falls
     * back to the normalized flat {@code address}. Every component is {@link #normalize normalized}, so
     * the result is stored, returned and compared in one canonical form.
     */
    public static String compose(String addressLine1, String addressLine2, String address) {
        String line1 = normalize(addressLine1);
        if (line1.isEmpty()) {
            return normalize(address);
        }
        String line2 = normalize(addressLine2);
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }

    /** Trimmed, whitespace-collapsed, upper-cased, with common abbreviations expanded. */
    public static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(expand(token));
        }
        return sb.toString();
    }

    private static String expand(String token) {
        switch (token) {
            case "ST":
                return "STREET";
            case "RD":
                return "ROAD";
            case "AVE":
                return "AVENUE";
            default:
                return token;
        }
    }
}
