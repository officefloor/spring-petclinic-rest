package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Canonical form of a postal address, applied whenever an owner is created: trim and collapse
 * whitespace, upper-case, and expand common abbreviations (ST-&gt;STREET, RD-&gt;ROAD, AVE-&gt;AVENUE).
 * Blank input (or input that is blank once trimmed) normalizes to {@code ""}. Used both to store the
 * address and for every address comparison (household duplicate detection and the shared household id).
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    public static String normalize(String raw) {
        String collapsed = (raw == null ? "" : raw).trim().replaceAll("\\s+", " ").toUpperCase();
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
