package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * The single-sourced definition of a normalized postal address, shared by the
 * create-time {@link NormalizeOwnerAddress} step (which stores the normalized value)
 * and {@link HouseholdKey} (so every address comparison uses the same normalized form).
 *
 * <p>Normalization trims the value, collapses any run of whitespace to a single space,
 * upper-cases it and expands common street-type abbreviations ({@code ST} &rarr;
 * {@code STREET}, {@code RD} &rarr; {@code ROAD}, {@code AVE} &rarr; {@code AVENUE}).
 *
 * <p>It also defines how the structured and flat address forms resolve (see
 * {@link #resolve(String, String, String)}): the structured fields are preferred, and
 * the canonical flat {@code address} is the normalized {@code addressLine1} with the
 * normalized {@code addressLine2} appended after a single space when the second line is
 * present. So downstream readers only ever see the one normalized flat form.
 */
final class OwnerAddresses {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddresses() {
    }

    /**
     * A resolved postal address: the normalized structured lines (each {@code null} when
     * absent) and the canonical, normalized flat {@code address} composed from them. The
     * flat {@code address} is {@code ""} only when the owner supplied no address in either
     * form.
     */
    record Normalized(String addressLine1, String addressLine2, String address) {
    }

    /**
     * Resolve an owner's supplied address fields into their canonical normalized form,
     * preferring the structured fields. When {@code addressLine1} is non-blank it (and
     * {@code addressLine2} when present) are normalized and the flat {@code address} is
     * composed from them — the normalized {@code addressLine1} with a single space and
     * the normalized {@code addressLine2} appended when the second line is present.
     * Otherwise the flat {@code address} is normalized and the structured lines are
     * {@code null} (backward compatible). The flat value is blank only when no address
     * was supplied in either form.
     */
    static Normalized resolve(String addressLine1, String addressLine2, String flatAddress) {
        String line1 = normalize(addressLine1);
        if (!line1.isBlank()) {
            String line2 = normalize(addressLine2);
            boolean hasLine2 = !line2.isBlank();
            return new Normalized(line1, hasLine2 ? line2 : null, hasLine2 ? line1 + " " + line2 : line1);
        }
        return new Normalized(null, null, normalize(flatAddress));
    }

    /**
     * Normalize an address. Returns {@code ""} when {@code address} is {@code null} or
     * is blank once whitespace has been trimmed and collapsed.
     */
    static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
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
