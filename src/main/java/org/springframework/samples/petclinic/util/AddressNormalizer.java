package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Map;

/**
 * Canonicalizes a postal address so it is stored and compared in one consistent form:
 * surrounding whitespace is trimmed, internal whitespace runs collapse to a single space,
 * letters are upper-cased, and common abbreviations are expanded to their full words.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /** Normalize {@code address}, or return it unchanged when {@code null}. */
    public static String normalize(String address) {
        if (address == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(address.length());
        for (String word : address.trim().toUpperCase(Locale.ROOT).split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(word, word));
        }
        return sb.toString();
    }
}
