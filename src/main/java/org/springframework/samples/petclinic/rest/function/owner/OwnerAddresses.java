package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

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
 */
public final class OwnerAddresses {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddresses() {
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
