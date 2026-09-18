package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Address normalization shared by the owner pipelines and the household identity: produces
 * an address's canonical form so it is stored, returned and compared in one consistent
 * representation. Normalizing means trimming and collapsing whitespace, upper-casing, and
 * expanding common street-type abbreviations to their full word.
 */
public final class OwnerAddress {

    /** Whole-token abbreviations expanded to their full word (compared upper-cased). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * The canonical form of {@code address}: trimmed, internal whitespace collapsed to single
     * spaces, upper-cased, with each abbreviation token expanded. A {@code null} address, or one
     * that is blank once trimmed, yields the empty string.
     */
    public static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
}
