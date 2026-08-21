package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared address handling for the owner pipelines. An address is normalized whenever an owner is
 * created: leading/trailing whitespace is trimmed, runs of whitespace are collapsed to a single
 * space, the text is upper-cased, and common street-type abbreviations are expanded
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). So {@code "  12  main  st "}
 * normalizes to {@code "12 MAIN STREET"}. The normalized form is what gets stored and returned, and
 * it is also the form used for every address comparison (household duplicate detection and the
 * shared household id).
 */
final class OwnerAddress {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Common street-type abbreviations expanded to their full word (keys are upper-cased tokens). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /** Normalizes the address on the request in place to its canonical form. */
    static void normalize(OwnerFieldsDto request) {
        request.setAddress(normalize(request.getAddress()));
    }

    /**
     * Canonical form of a raw address: trimmed, whitespace-collapsed, upper-cased, with common
     * street-type abbreviations expanded. Returns {@code ""} for a null or whitespace-only value.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = WHITESPACE.matcher(value.trim()).replaceAll(" ");
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.toUpperCase().split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (String token : tokens) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }
}
