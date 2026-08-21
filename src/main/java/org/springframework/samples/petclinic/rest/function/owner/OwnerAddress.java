package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared address handling for the owner pipelines. An owner may supply its address in either form:
 * the STRUCTURED fields ({@code addressLine1} plus an optional {@code addressLine2}) or the flat
 * {@code address} kept for backward compatibility. Whichever fields are supplied are normalized:
 * leading/trailing whitespace is trimmed, runs of whitespace are collapsed to a single space, the
 * text is upper-cased, and common street-type abbreviations are expanded ({@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}). So {@code "  12  main  st "} normalizes to
 * {@code "12 MAIN STREET"}.
 *
 * <p>The structured fields are preferred when present. When {@code addressLine1} is non-blank the
 * composed {@code address} is the normalized {@code addressLine1}, with a single space and the
 * normalized {@code addressLine2} appended when {@code addressLine2} is present; otherwise the flat
 * {@code address} is used. The composed/normalized {@code address} is what gets stored and returned,
 * and it is also the form used for every address comparison.
 */
final class OwnerAddress {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Common street-type abbreviations expanded to their full word (keys are upper-cased tokens). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private OwnerAddress() {
    }

    /**
     * Normalizes the address on the request in place to its canonical form. The structured lines are
     * normalized (cleared when blank), and the flat {@code address} is set to the composed form: when
     * a non-blank {@code addressLine1} is supplied it is preferred - {@code address} becomes the
     * normalized {@code addressLine1}, with a single space and the normalized {@code addressLine2}
     * appended when {@code addressLine2} is present - otherwise the flat {@code address} is normalized
     * as before.
     */
    static void normalize(OwnerFieldsDto request) {
        String line1 = normalize(request.getAddressLine1());
        String line2 = normalize(request.getAddressLine2());
        request.setAddressLine1(line1.isEmpty() ? null : line1);
        request.setAddressLine2(line2.isEmpty() ? null : line2);
        if (!line1.isEmpty()) {
            // Prefer the structured fields: compose the address from the normalized lines.
            request.setAddress(line2.isEmpty() ? line1 : line1 + " " + line2);
        }
        else {
            // Backward-compatible flat form.
            request.setAddress(normalize(request.getAddress()));
        }
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
