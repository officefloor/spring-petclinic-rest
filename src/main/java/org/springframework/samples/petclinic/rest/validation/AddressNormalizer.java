package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Normalizes owner postal addresses into the single canonical form that is stored, returned and
 * compared when an owner is created. Keeping this logic in one place lets the create endpoint treat
 * an address as an opaque, already-canonical value: it {@link #normalize(String) normalizes} an
 * incoming address once, then relies on that same form for the required-field check, household
 * duplicate detection and the shared household id.
 *
 * <p>Normalization trims and collapses whitespace, upper-cases the result (using
 * {@link Locale#ROOT} so it is locale-independent) and expands a small set of common street-type
 * abbreviations applied to whole words only ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). For example {@code "  12  main  st "} becomes {@code "12 MAIN STREET"}.
 */
@Component
public class AddressNormalizer {

    /** Whole-word abbreviation -> expansion, matched against upper-cased tokens. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalizes an address submitted to the create endpoint: leading and trailing whitespace is
     * trimmed, every run of internal whitespace is collapsed to a single space, the result is
     * upper-cased and common street-type abbreviations are expanded on a whole-word basis. A
     * {@code null} address is left as-is; a value that is blank (whitespace-only) collapses to an
     * empty string.
     *
     * @param address the raw address value from the request, or {@code null} when omitted
     * @return the normalized address to store and return, or {@code null} when none was supplied
     */
    public String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
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
