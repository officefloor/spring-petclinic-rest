package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Canonical address normalization for the owner pipelines. Applied once when an owner is created
 * or updated so the stored and returned {@code address} is canonical, and reused for every
 * address comparison (household duplicate detection in {@link Household} and the shared household
 * id) so all steps compare the same form.
 *
 * <p>An owner may supply its address in either of two forms, the structured fields preferred when
 * present: the structured {@code addressLine1} (with an optional {@code addressLine2}), or the
 * flat {@code address}. {@link #apply(OwnerFieldsDto)} normalizes whichever fields are supplied
 * and computes the composed {@code address}.
 *
 * <p>Normalization: trim, collapse any run of whitespace to a single space, upper-case, and
 * expand common abbreviations as whole words — {@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}. Yields {@code ""} for a {@code null} or whitespace-only input.
 */
final class OwnerAddress {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * Normalize the request's address fields in place and compute the composed {@code address}.
     * The structured form is preferred when a non-blank {@code addressLine1} is supplied: both
     * {@code addressLine1} and (when present) {@code addressLine2} are normalized, and the
     * composed {@code address} is the normalized {@code addressLine1} with a single space and the
     * normalized {@code addressLine2} appended when the latter is present. Otherwise the flat
     * {@code address} is normalized and left as the address. Blank/absent fields normalize to
     * {@code ""}, so the required-field check that follows sees the canonical form.
     */
    static void apply(OwnerFieldsDto request) {
        if (isPresent(request.getAddressLine1())) {
            String line1 = normalize(request.getAddressLine1());
            request.setAddressLine1(line1);
            String line2 = normalize(request.getAddressLine2());
            if (line2.isEmpty()) {
                request.setAddressLine2(null);
                request.setAddress(line1);
            }
            else {
                request.setAddressLine2(line2);
                request.setAddress(line1 + " " + line2);
            }
        }
        else {
            request.setAddress(normalize(request.getAddress()));
        }
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
