package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

/**
 * Canonical form of an owner address, applied on create and used for every address comparison
 * (household duplicate detection and the shared household id). Whitespace is trimmed and collapsed,
 * the text is upper-cased and common street-type abbreviations are expanded (ST->STREET, RD->ROAD,
 * AVE->AVENUE). A null or all-whitespace address normalizes to {@code ""}.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS =
            Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    public static String normalize(String address) {
        if (address == null || address.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String word : address.trim().toUpperCase().split("\\s+")) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(word, word));
        }
        return sb.toString();
    }
}
