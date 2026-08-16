package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalizes the address of the validated body (published by {@link RequireOwnerFields}) into
 * a canonical form: leading/trailing whitespace is trimmed, internal whitespace runs are
 * collapsed to a single space, the whole value is upper-cased, and common abbreviations are
 * expanded to their full word ({@code ST}&rarr;{@code STREET}, {@code RD}&rarr;{@code ROAD},
 * {@code AVE}&rarr;{@code AVENUE}). The canonical value is written back onto the body in place,
 * so {@link BuildOwner} stores it and later reads/responses return it.
 *
 * <p>Normalization applies to whichever address fields the body supplied: the structured
 * {@code addressLine1}/{@code addressLine2} are preferred when present, otherwise the flat
 * {@code address}. When a structured {@code addressLine1} is supplied the flat {@code address} is
 * replaced with the composed form — the normalized {@code addressLine1} followed, when an
 * {@code addressLine2} is present, by a single space and the normalized {@code addressLine2}.
 *
 * <p>Runs after {@link RequireOwnerFields} — which already rejects a body with no address in
 * either form — and before the household steps, so
 * {@link RequireUniqueOwnerHousehold duplicate detection} and
 * {@link AssignHouseholdId the shared household id} both compare the normalized form.
 */
public class NormalizeOwnerAddress {

    public void service(@Val OwnerFieldsDto request) {
        String line1 = normalize(request.getAddressLine1());
        String line2 = normalize(request.getAddressLine2());
        if (!line1.isEmpty()) {
            // Structured address supplied: normalize the lines and compose the flat address.
            request.setAddressLine1(line1);
            request.setAddressLine2(line2.isEmpty() ? null : line2);
            request.setAddress(compose(line1, line2));
        }
        else {
            request.setAddress(normalize(request.getAddress()));
        }
    }

    /**
     * The composed address: the normalized {@code addressLine1}, with a single space and the
     * normalized {@code addressLine2} appended when the latter is present (non-blank).
     */
    static String compose(String line1, String line2) {
        return line2.isEmpty() ? line1 : line1 + " " + line2;
    }

    /**
     * Trimmed, internal whitespace collapsed to a single space, upper-cased, with common
     * abbreviations expanded to their full word. Returns {@code ""} for a null or blank value.
     * Shared with {@link RequireOwnerFields} so the required-field check rejects an address
     * that is blank after normalization.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expand(tokens[i]));
        }
        return sb.toString();
    }

    /** Expands a single upper-cased token when it is a known abbreviation, else returns it. */
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
