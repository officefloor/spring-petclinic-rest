package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Normalizes a raw address to the stored/returned form: trims and collapses whitespace,
 * upper-cases, and expands common abbreviations (ST->STREET, RD->ROAD, AVE->AVENUE).
 * Returns "" for a null or blank-after-trim address so callers can reject it as missing.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREV = Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String word : value.trim().toUpperCase().split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREV.getOrDefault(word, word));
        }
        return sb.toString();
    }
}
