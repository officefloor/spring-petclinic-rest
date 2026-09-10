package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Normalization for the owner {@code address}, applied whenever an owner is created.
 *
 * <p>Rules: trim the ends and collapse runs of whitespace to a single space, upper-case, and
 * expand common street-type abbreviations ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). So {@code "  12  main  st "} becomes {@code "12 MAIN STREET"} and
 * {@code "7 elm ave"} becomes {@code "7 ELM AVENUE"}.
 *
 * <p>The normalized value is what {@link ValidateOwnerFields} stores on the request (so it is
 * persisted and returned) and is the form used for every address comparison — the required-field
 * check (a value blank after normalization is rejected), the duplicate identityKey
 * ({@link EnsureUniqueOwnerIdentity}) and the shared household id ({@link AssignOwnerHousehold}).
 * The transformation is idempotent, so re-normalizing an already-normalized value is a no-op.
 */
final class OwnerAddress {

    /** Common street-type abbreviations expanded to their full (upper-cased) form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * Normalize a raw address; {@code null} stays {@code null} and a value that is only whitespace
     * normalizes to the empty string.
     */
    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
