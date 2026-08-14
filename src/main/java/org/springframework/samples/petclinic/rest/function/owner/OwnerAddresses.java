package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared address normalization for the create-owner pipeline. A single canonical form is used both
 * for what is stored and returned and for every address comparison (household duplicate detection
 * and the shared household id), so that inputs differing only in surrounding/repeated whitespace,
 * letter case or a common street-type abbreviation are treated as the same address.
 *
 * <p>Rules: trim, collapse internal whitespace runs to a single space, upper-case, then expand the
 * common street-type abbreviations {@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE} as
 * whole space-separated words. The transform is idempotent. A {@code null} or all-whitespace input
 * normalizes to the empty string, so the create-time required-field check rejects it.
 *
 * <p>An owner may supply its address either structured ({@code addressLine1} plus an optional
 * {@code addressLine2}) or flat ({@code address}). {@link #applyTo} normalizes whichever fields are
 * present in place and composes the stored/returned flat {@code address}: the structured form is
 * preferred, so when a non-blank {@code addressLine1} is given the composed address is the
 * normalized {@code addressLine1} with a single space and the normalized {@code addressLine2}
 * appended when present; otherwise the flat {@code address} input is used (backward-compatible).
 */
public final class OwnerAddresses {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddresses() {
    }

    /**
     * Normalize the request's address fields in place and set the composed flat {@code address}.
     * Structured {@code addressLine1}/{@code addressLine2} are preferred when present; otherwise the
     * flat {@code address} input is kept. After this runs a blank {@code address} means the request
     * supplied an address in neither form, so the required-field check rejects it.
     */
    public static void applyTo(OwnerFieldsDto request) {
        String line1 = normalize(request.getAddressLine1());
        String line2 = normalize(request.getAddressLine2());
        if (!line1.isEmpty()) {
            request.setAddressLine1(line1);
            request.setAddressLine2(line2.isEmpty() ? null : line2);
            request.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
        }
        else {
            request.setAddressLine1(null);
            request.setAddressLine2(null);
            request.setAddress(normalize(request.getAddress()));
        }
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }
}
