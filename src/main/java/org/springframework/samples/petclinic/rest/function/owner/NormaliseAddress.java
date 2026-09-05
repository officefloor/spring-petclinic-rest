package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical form of a street address: trim and collapse internal whitespace, upper-case,
 * and expand common abbreviations (ST->STREET, RD->ROAD, AVE->AVENUE) as whole words.
 * Returns {@code ""} for a null or all-whitespace input, so a blank address is rejected
 * by the required-field check. Idempotent, so it can be reapplied wherever addresses are
 * compared.
 */
public final class NormaliseAddress {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private NormaliseAddress() {
    }

    public static String normalise(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }
}
